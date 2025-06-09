package au.com.dougboyd.rumpelstiltskin.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.ChatModel;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

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
    public String generateCodeDirectly(@RequestParam String inputRequest) throws IOException, InterruptedException {
        // 1) Build a ChatCompletion request
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_3_5_TURBO)
                .addUserMessage(inputRequest)
                .build();

        // 2) Send it to ChatGPT
        ChatCompletion chatCompletion = openAIClient
                .chat()
                .completions()
                .create(params);
        String diff = chatCompletion
                .choices()
                .get(0)
                .message()
                .content();

        // 3) Write the returned diff to a patch file
        Path patchFile = Paths.get(rootDirectory, "changes.patch");
        Files.writeString(patchFile, diff, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // 4) Apply the patch via the system 'patch' tool
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