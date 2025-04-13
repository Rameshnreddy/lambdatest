package Utilities;

import static Utilities.DriverFactory.getDriver;
import static Utilities.DriverFactory.getTest;
import static org.testng.util.Strings.escapeHtml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class FixWithAIPrompt implements ITestListener {
    private static final String AI_PROMPT_FOLDER = "AIPrompt/ai-suggested-prompt/";

    private static final String PROMPT_TEMPLATE = "You are an expert in Selenium testing with Java. \n"
            + "Fix the error in the Selenium test \"{title}\". \n"
            + "- Start response with a highlighted diff of fixed code snippet.\n"
            + "- Strictly rely on the HTML structure of the page.\n" + "- Avoid adding any new code.\n"
            + "- Avoid adding comments to the code.\n" + "- Avoid changing the test logic.\n"
            + "- Try to use more robust selectors like CSS or XPath that match the page structure.\n"
            + "- Add concise notes about applied changes at the end of your response.\n"
            + "- If the test may be correct and there is a bug in the page, note it.\n\n" + "{error}\n\n"
            + "Code snippet of the failing test:\n\n" + "{snippet}\n\n" + "HTML structure of the page:\n\n"
            + "{pageSource}";

    @Override
    public void onTestFailure(ITestResult result) {
        WebDriver driver = getDriver();
        if (driver != null) {
            String testName = result.getMethod().getMethodName();
            Throwable error = result.getThrowable();
            String pageSource = getAccessiblePageStructure(driver);
            boolean fixWithAIPrompt = Boolean.parseBoolean(System.getProperty("enableFixWithAIPromptFeature", "false"));
            //change later
            if (true) {
                String prompt = buildPrompt(testName, error, pageSource);
                // Save the AI fix prompt to a file
                saveFixPrompt(result, prompt);
                // Create a collapsible prompt with copy to clipboard functionality
                String collapsiblePromptWithClipboard = createCollapsiblePromptWithClipboard(prompt);

                getTest().info(collapsiblePromptWithClipboard);
            }
        }
    }

    private String createCollapsiblePromptWithClipboard(String prompt) {
        // Generate a unique ID for this specific test failure
        String uniqueId = "ai-prompt-" + System.currentTimeMillis() + "-" + Math.random();

        // HTML for a collapsible section with a copy button
        return "<details>" +
                "<summary>🤖 AI Prompt Suggestion (Click to Expand)</summary>" +
                "<div style='position: relative; background-color: #f4f4f4; padding: 10px; border: 1px solid #ddd;'>" +
                "<button onclick='copyToClipboard(this, \"" + uniqueId + "\")' " +
                "style='position: absolute; top: 5px; right: 5px; background-color: #4CAF50; " +
                "color: white; border: none; padding: 5px 10px; cursor: pointer;'>" +
                "Copy Prompt</button>" +
                "<pre id='" + uniqueId + "' style='white-space: pre-wrap; word-wrap: break-word;'>" +
                escapeHtml(prompt) + "</pre>" +
                "</div>" +
                "</details>" +
                "<script>" +
                "function copyToClipboard(btn, elementId) {" +
                "   var copyText = document.getElementById(elementId);" +
                "   var textArea = document.createElement('textarea');" +
                "   textArea.value = copyText.textContent;" +
                "   document.body.appendChild(textArea);" +
                "   textArea.select();" +
                "   document.execCommand('copy');" +
                "   document.body.removeChild(textArea);" +
                "   btn.textContent = 'Copied!';" +
                "   setTimeout(() => { btn.textContent = 'Copy Prompt'; }, 2000);" +
                "}" +
                "</script>";
    }

    /*
     *
     * Get a simplified version of the page structure focused on accessibility
     * (Similar to ARIA snapshot in Playwright but simplified for Selenium)
     *
     */

    private String getAccessiblePageStructure(WebDriver driver) {
        // Existing implementation remains the same
        // Just using the passed driver parameter instead of a class-level driver
        //new js code for handling shadow DOM
        String js2 = "function getAccessibleTree(element, depth = 0) {\n" +
                "  if (!element) return '';\n" +
                "  \n" +
                "  const indent = ' '.repeat(depth * 2);\n" +
                "  let roleInfo = element.tagName.toLowerCase();\n" +
                "  \n" +
                "  // Add role information\n" +
                "  const role = element.getAttribute('role');\n" +
                "  if (role) roleInfo += ` role=\"${role}\"`;\n" +
                "  \n" +
                "  // Add important attributes\n" +
                "  const importantAttrs = ['id', 'name', 'type', 'aria-label', 'aria-labelledby', 'title', 'alt', 'placeholder'];\n" +
                "  importantAttrs.forEach(attr => {\n" +
                "    const value = element.getAttribute(attr);\n" +
                "    if (value) roleInfo += ` ${attr}=\"${value}\"`;\n" +
                "  });\n" +
                "  \n" +
                "  // Add text content if it exists and is relevant\n" +
                "  const textContent = element.textContent.trim();\n" +
                "  if (textContent && element.children.length === 0) {\n" +
                "    // Truncate long text\n" +
                "    const displayText = textContent.length > 50 ? textContent.substring(0, 47) + '...' : textContent;\n" +
                "    roleInfo += `: \"${displayText}\"`;\n" +
                "  }\n" +
                "  \n" +
                "  let result = `${indent}<${roleInfo}>`;\n" +
                "  \n" +
                "  // Process children\n" +
                "  for (const child of element.children) {\n" +
                "    const childTree = getAccessibleTree(child, depth + 1);\n" +
                "    if (childTree) result += '\\n' + childTree;\n" +
                "  }\n" +
                "  \n" +
                "  // Explicitly fetch and process Nested Shadow DOM\n" +
                "  function processNestedShadowDOM(element, currentDepth) {\n" +
                "    let shadowResult = '';\n" +
                "    \n" +
                "    // Find all potential shadow hosts\n" +
                "    const potentialShadowHosts = element.querySelectorAll('*');\n" +
                "    \n" +
                "    potentialShadowHosts.forEach(host => {\n" +
                "      if (host.shadowRoot) {\n" +
                "        shadowResult += `\\n${' '.repeat(currentDepth * 2)}  <nested-shadow-root id=\"${host.id || 'unknown'}\">`;\n" +
                "        \n" +
                "        // Recursively process shadow root\n" +
                "        const shadowRootTree = getAccessibleTree(host.shadowRoot, currentDepth + 2);\n" +
                "        if (shadowRootTree) shadowResult += '\\n' + shadowRootTree;\n" +
                "        \n" +
                "        // Try to get shadow root inner content\n" +
                "        try {\n" +
                "          const shadowInnerHTML = host.shadowRoot.innerHTML.trim();\n" +
                "          if (shadowInnerHTML) {\n" +
                "            shadowResult += `\\n${' '.repeat(currentDepth * 2)}    <shadow-content-length>${shadowInnerHTML.length} chars</shadow-content-length>`;\n" +
                "          }\n" +
                "        } catch (error) {\n" +
                "          shadowResult += `\\n${' '.repeat(currentDepth * 2)}    <shadow-access-error>Content inaccessible</shadow-access-error>`;\n" +
                "        }\n" +
                "      }\n" +
                "    });\n" +
                "    \n" +
                "    return shadowResult;\n" +
                "  }\n" +
                "  \n" +
                "  // Add Nested Shadow DOM processing\n" +
                "  result += processNestedShadowDOM(element, depth);\n" +
                "  \n" +
                "  return result;\n" +
                "}\n" +
                "\n" +
                "return getAccessibleTree(document.body);";
        String js1 = "function getAccessibleTree(element, depth = 0) {\n" +
                "  if (!element) return '';\n" +
                "  \n" +
                "  const indent = ' '.repeat(depth * 2);\n" +
                "  let roleInfo = element.tagName.toLowerCase();\n" +
                "  \n" +
                "  // Add role information\n" +
                "  const role = element.getAttribute('role');\n" +
                "  if (role) roleInfo += ` role=\"${role}\"`;\n" +
                "  \n" +
                "  // Add important attributes\n" +
                "  const importantAttrs = ['id', 'name', 'type', 'aria-label', 'aria-labelledby', 'title', 'alt', 'placeholder'];\n" +
                "  importantAttrs.forEach(attr => {\n" +
                "    const value = element.getAttribute(attr);\n" +
                "    if (value) roleInfo += ` ${attr}=\"${value}\"`;\n" +
                "  });\n" +
                "  \n" +
                "  // Add text content if it exists and is relevant\n" +
                "  const textContent = element.textContent.trim();\n" +
                "  if (textContent && element.children.length === 0) {\n" +
                "    // Truncate long text\n" +
                "    const displayText = textContent.length > 50 ? textContent.substring(0, 47) + '...' : textContent;\n" +
                "    roleInfo += `: \"${displayText}\"`;\n" +
                "  }\n" +
                "  \n" +
                "  let result = `${indent}<${roleInfo}>`;\n" +
                "  \n" +
                "  // Process children\n" +
                "  for (const child of element.children) {\n" +
                "    const childTree = getAccessibleTree(child, depth + 1);\n" +
                "    if (childTree) result += '\\n' + childTree;\n" +
                "  }\n" +
                "  \n" +
                "  // Explicitly fetch and process Shadow DOM\n" +
                "  function processShadowDOM(element) {\n" +
                "    const shadowHosts = element.querySelectorAll('*');\n" +
                "    let shadowResult = '';\n" +
                "    \n" +
                "    shadowHosts.forEach(host => {\n" +
                "      if (host.shadowRoot) {\n" +
                "        shadowResult += `\\n${indent}  <shadow-root id=\"${host.id || 'unknown'}\">`;\n" +
                "        \n" +
                "        // Traverse shadow root children\n" +
                "        const shadowChildren = host.shadowRoot.children;\n" +
                "        for (const shadowChild of shadowChildren) {\n" +
                "          const shadowChildTree = getAccessibleTree(shadowChild, depth + 2);\n" +
                "          if (shadowChildTree) shadowResult += '\\n' + shadowChildTree;\n" +
                "        }\n" +
                "        \n" +
                "        // Attempt to get shadow root inner HTML\n" +
                "        try {\n" +
                "          const shadowInnerHTML = host.shadowRoot.innerHTML.trim();\n" +
                "          if (shadowInnerHTML) {\n" +
                "            shadowResult += `\\n${indent}    <shadow-content>${shadowInnerHTML}</shadow-content>`;\n" +
                "          }\n" +
                "        } catch (error) {\n" +
                "          shadowResult += `\\n${indent}    <shadow-error>Could not access Shadow DOM content</shadow-error>`;\n" +
                "        }\n" +
                "      }\n" +
                "    });\n" +
                "    \n" +
                "    return shadowResult;\n" +
                "  }\n" +
                "  \n" +
                "  // Add Shadow DOM processing to the result\n" +
                "  result += processShadowDOM(element);\n" +
                "  \n" +
                "  return result;\n" +
                "}\n" +
                "\n" +
                "return getAccessibleTree(document.body);";
//skip invisible elements wiht limited html tree strucutre
        String jsnew = "function getAccessibleTree(element, depth = 0, maxDepth = 5) {\n" +
                "  // Stop at maximum depth to prevent overwhelming output\n" +
                "  if (!element || depth > maxDepth) return '';\n" +
                "  \n" +
                "  const indent = ' '.repeat(depth * 2);\n" +
                "  let roleInfo = element.tagName.toLowerCase();\n" +
                "  \n" +
                "  // Only include essential attributes\n" +
                "  const essentialAttrs = ['id', 'name', 'type'];\n" +
                "  essentialAttrs.forEach(attr => {\n" +
                "    const value = element.getAttribute(attr);\n" +
                "    if (value) roleInfo += ` ${attr}=\"${value}\"`;\n" +
                "  });\n" +
                "  \n" +
                "  // Add text content only if it's a leaf node with text\n" +
                "  const textContent = element.textContent.trim();\n" +
                "  if (textContent && element.children.length === 0) {\n" +
                "    const displayText = textContent.length > 30 ? textContent.substring(0, 27) + '...' : textContent;\n" +
                "    roleInfo += `: \"${displayText}\"`;\n" +
                "  }\n" +
                "  \n" +
                "  let result = `${indent}<${roleInfo}>`;\n" +
                "  \n" +
                "  // Only process direct children (no deep recursion for all descendants)\n" +
                "  for (const child of element.children) {\n" +
                "    // Skip invisible elements\n" +
                "    const style = window.getComputedStyle(child);\n" +
                "    if (style.display === 'none' || style.visibility === 'hidden') continue;\n" +
                "    \n" +
                "    const childTree = getAccessibleTree(child, depth + 1, maxDepth);\n" +
                "    if (childTree) result += '\\n' + childTree;\n" +
                "  }\n" +
                "  \n" +
                "  return result;\n" +
                "}\n" +
                "\n" +
                "return getAccessibleTree(document.body, 0, 4);";
        ///old code

        String js = "function getAccessibleTree(element, depth = 0) {\n" + "  if (!element) return '';\n" + "  \n"
                + "  const indent = ' '.repeat(depth * 2);\n" + "  let roleInfo = element.tagName.toLowerCase();\n"
                + "  \n" + "  // Add role information\n" + "  const role = element.getAttribute('role');\n"
                + "  if (role) roleInfo += ` role=\"${role}\"`;\n" + "  \n" + "  // Add important attributes\n"
                + "  const importantAttrs = ['id', 'name', 'type', 'aria-label', 'aria-labelledby', 'title', 'alt', 'placeholder'];\n"
                + "  importantAttrs.forEach(attr => {\n" + "    const value = element.getAttribute(attr);\n"
                + "    if (value) roleInfo += ` ${attr}=\"${value}\"`;\n" + "  });\n" + "  \n"
                + "  // Add text content if it exists and is relevant\n"
                + "  const textContent = element.textContent.trim();\n"
                + "  if (textContent && element.children.length === 0) {\n" + "    // Truncate long text\n"
                + "    const displayText = textContent.length > 50 ? textContent.substring(0, 47) + '...' : textContent;\n"
                + "    roleInfo += `: \"${displayText}\"`;\n" + "  }\n" + "  \n"
                + "  let result = `${indent}<${roleInfo}>`;\n" + "  \n" + "  // Process children\n"
                + "  for (const child of element.children) {\n"
                + "    const childTree = getAccessibleTree(child, depth + 1);\n"
                + "    if (childTree) result += '\\n' + childTree;\n" + "  }\n" + "  \n" + "  return result;\n" + "}\n"
                + "\n" + "return getAccessibleTree(document.body);";
        try {
            Object result = ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(js1);
            return result != null ? result.toString() : "Could not retrieve page structure";
        } catch (Exception e) {
            // Fallback to basic page source if JavaScript execution fails
            return driver.getPageSource();
        }
    }

    private String buildPrompt(String title, Throwable error, String pageSource) {
        String errorMessage = getStackTraceAsString(error).split("\n")[0];
        String snippet = getCodeSnippet(error);
        if (errorMessage.isEmpty() || snippet.isEmpty()) {
            return "";
        }
        return PROMPT_TEMPLATE.replace("{title}", title).replace("{error}", errorMessage).replace("{snippet}", snippet)
                .replace("{pageSource}", pageSource);
    }

    private String getStackTraceAsString(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    private String getCodeSnippet(Throwable error) {
        StackTraceElement[] stackTrace = error.getStackTrace();
        if (stackTrace.length == 0) {
            return "";
        }
        // Find the first stack element that's in our test code
        StackTraceElement testElement = null;
        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().contains("lambdatestHackathon.")) {
                testElement = element;
//                break;
            }
        }
        if (testElement == null) {
            return "";
        }
        String fileName = testElement.getFileName();
        int lineNumber = testElement.getLineNumber();
        // Convert class name to file path
        String filePath = getSourceFilePath(testElement.getClassName(), fileName);
        if (filePath == null || !new File(filePath).exists()) {
            return "// Could not locate source file: " + fileName;
        }
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
            // Extract relevant lines (3 before and 4 after)
            int startLine = Math.max(0, lineNumber - 4);
            int endLine = Math.min(lines.size() - 1, lineNumber + 3);
            StringBuilder snippetBuilder = new StringBuilder();
            for (int i = startLine; i <= endLine; i++) {
                snippetBuilder.append(lines.get(i)).append("\n");
            }
            return snippetBuilder.toString();
        } catch (IOException e) {
            return "// Failed to read source file: " + e.getMessage();
        }
    }

    private String getSourceFilePath(String className, String fileName) {
        // This is a heuristic approach to find the source file
        // It might need adjustment based on your project structure
        // Try common source directories
        String[] sourceDirs = {"src/test/java", "src/main/java", "src", "test", "."};
        String packagePath = className.substring(0, className.lastIndexOf('.')).replace('.', File.separatorChar);
        for (String dir : sourceDirs) {
            String path = dir + File.separator + packagePath + File.separator + fileName;
            if (new File(path).exists()) {
                return path;
            }
        }
        return null;
    }

    private String getActualTestClassName(ITestResult result) {
        // Get the actual test class name
        Class<?> testClass = result.getTestClass().getRealClass();
        return testClass.getSimpleName();
    }

    private void saveFixPrompt(ITestResult result, String prompt) {
        String testClassName = getActualTestClassName(result);
        String testMethodName = result.getMethod().getMethodName();

        String fileName = testClassName + "-" + testMethodName + "-prompt-suggestion.txt";
        String filePath = AI_PROMPT_FOLDER + fileName;
        try {
            // Ensure directory exists
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            // Write to file
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(prompt);
            }
        } catch (IOException e) {
            System.err.println("Failed to save AI fix prompt: " + e.getMessage());
        }
    }
//
//	private String getFixPromptFilePath(String testName) {
//		return AI_PROMPT_FOLDER + getTestClassName() + "-" + testName + "-prompt-suggestion.txt";
//	}

    public static String getTestClassName() {
        return Thread.currentThread().getStackTrace()[2].getClassName();
    }

    public static void cleanupAIFixFolder() {
        try {
            Path directoryPath = Paths.get(AI_PROMPT_FOLDER);

            // Check if directory exists
            if (!Files.exists(directoryPath)) {
                return;
            }

            // Walk through the directory and delete all files and subdirectories
            Files.walk(directoryPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(file -> {
                        if (!file.getPath().equals(directoryPath.toFile().getPath())) {
                            try {
                                Files.delete(file.toPath());
                            } catch (IOException e) {
                                System.err.println("Error deleting file: " + file.getPath() + " - " + e.getMessage());
                            }
                        }
                    });

//            System.out.println("AI Fix folder cleaned up successfully.");
        } catch (IOException e) {
            System.err.println("Error cleaning up AI Fix folder: " + e.getMessage());
        }
    }
}
