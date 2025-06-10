package au.com.dougboyd.rumpelstiltskin.controller;

import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Controller for simple greetings and AI-driven code generation.
 */
@RestController
@RequestMapping("/api")
public class HelloController {

    private final OpenAIClient openAIClient;

    @Value("${root.directory}")
    private String rootDirectory;

    public HelloController(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
    }

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello, Rumpelstiltskin!";
    }

    @PostMapping("/generate-code-directly")
    public String generateCodeDirectly(@RequestParam String inputRequest)
            throws IOException, InterruptedException {

        // 1) Build a ChatCompletion request with a system prompt
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_3_5_TURBO)
                .addSystemMessage("You are a Git patch generator. "
                        + "Always respond with a valid unified diff (starting with 'diff --git'). "
                        + "Do NOT include any explanations or extra text.")
                .addUserMessage(inputRequest)
                .build();

        // 2) Send it to ChatGPT
        ChatCompletion chatCompletion = openAIClient
                .chat()
                .completions()
                .create(params);

        // 3) Extract and unwrap the diff
        String diff = chatCompletion
                .choices()
                .get(0)
                .message()
                .content()
                .orElse("");

        // 4) Validate it looks like a Git diff
        if (!diff.startsWith("diff --git")) {
            return "AI did not return a valid patch. Received:\n" + diff;
        }

        // 5) Write the diff to a patch file
        Path patchFile = Paths.get(rootDirectory, "changes.patch");
        Files.writeString(patchFile, diff, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // 6) Apply the patch via the 'patch' tool
        ProcessBuilder pb = new ProcessBuilder("patch", "-p1", "-i", patchFile.toString());
        pb.directory(new File(rootDirectory));
        Process proc = pb.start();
        int exitCode = proc.waitFor();

        if (exitCode != 0) {
            String err = new String(proc.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            return "Patch failed:\n" + err;
        }

        return "Patch applied successfully:\n" + diff;
    }
}