package com.nfr;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the one piece of wiring nothing else covers: template.yaml names the
 * Lambda entry point as a plain string, so renaming the class or the method
 * leaves the rest of the suite green and breaks the deploy. Resolving that
 * string reflectively makes a rename fail here instead of in CloudFormation.
 *
 * The template is read line-by-line rather than parsed as YAML on purpose —
 * it carries CloudFormation intrinsic tags (`!Sub`) that a plain YAML parser
 * rejects, and this needs no dependency beyond the JDK.
 */
class TemplateHandlerWiringTest {

    private static final Path TEMPLATE = Path.of("template.yaml");

    /** Matches `Handler: com.nfr.MyApiHandler::handleRequest`, trailing comment allowed. */
    private static final Pattern HANDLER_LINE =
            Pattern.compile("^\\s*Handler:\\s*([\\w.]+)::(\\w+)\\s*(?:#.*)?$");

    private static List<Matcher> handlerDeclarations() throws IOException {
        assertThat(TEMPLATE)
                .as("SAM template, resolved against the Gradle test working directory")
                .exists();

        try (Stream<String> lines = Files.lines(TEMPLATE)) {
            return lines.map(HANDLER_LINE::matcher).filter(Matcher::matches).toList();
        }
    }

    private static Matcher theHandler() throws IOException {
        return handlerDeclarations().getFirst();
    }

    @Test
    void templateDeclaresExactlyOneHandler() throws IOException {
        assertThat(handlerDeclarations())
                .as("`Handler:` entries in %s — the tests below assume a single function", TEMPLATE)
                .hasSize(1);
    }

    @Test
    void theDeclaredHandlerClassResolvesAndImplementsRequestHandler() throws Exception {
        Class<?> handlerClass = Class.forName(theHandler().group(1));

        assertThat(RequestHandler.class).isAssignableFrom(handlerClass);
    }

    @Test
    void theDeclaredHandlerMethodExistsWithTheLambdaSignature() throws Exception {
        Matcher handler = theHandler();
        Class<?> handlerClass = Class.forName(handler.group(1));

        Method method = handlerClass.getMethod(handler.group(2), Map.class, Context.class);

        assertThat(method.getReturnType()).isEqualTo(Map.class);
    }

    @Test
    void theDeclaredHandlerClassIsInstantiableByTheLambdaRuntime() throws Exception {
        Class<?> handlerClass = Class.forName(theHandler().group(1));

        // The runtime constructs the handler through its public no-arg constructor.
        Object instance = handlerClass.getDeclaredConstructor().newInstance();

        assertThat(instance).isInstanceOf(RequestHandler.class);
    }
}
