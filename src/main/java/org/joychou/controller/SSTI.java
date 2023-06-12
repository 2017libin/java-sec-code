package org.joychou.controller;


import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;

@RestController
@RequestMapping("/ssti")
public class SSTI {
    /**
     * SSTI of Java velocity. The latest Velocity version still has this problem.
     * Fix method: Avoid to use Velocity.evaluate method.
     * <p>
     * http://localhost:7077/ssti/velocity/evaluate?template=%23set($e=%22e%22);$e.getClass().forName(%22java.lang.Runtime%22).getMethod(%22getRuntime%22,null).invoke(null,null).exec(%22open%20-a%20Calculator%22)
     * Open a calculator in MacOS.
     *
     * @param template exp
     */

    // payload in windows: http://localhost:7077/ssti/velocity/evaluate?template=%23set($e=%22e%22);$e.getClass().forName(%22java.lang.Runtime%22).getMethod(%22getRuntime%22,null).invoke(null,null).exec(%22calc%22)
    @GetMapping("/velocity/evaluate")
    public void velocityEvaluate(String template) {
        Velocity.init();

        VelocityContext context = new VelocityContext();

        context.put("author", "Elliot A.");
        context.put("address", "217 E Broadway");
        context.put("phone", "555-1337");

        StringWriter swOut = new StringWriter();
        Velocity.evaluate(context, swOut, "test", template);

        // 获取结果并输出
        String evaluatedResult = swOut.toString();
        System.out.println(evaluatedResult);
    }

    // payload in windows: http://localhost:7077/ssti/velocity/merge
    @GetMapping("/velocity/merge")
    public void velocity2() throws IOException, ParseException, org.apache.velocity.runtime.parser.ParseException {
        ClassPathResource classPathResource = new ClassPathResource("templates/vuln.vm");
        InputStream in = classPathResource.getInputStream();
        byte[] bytes = new byte[in.available()];
        in.read(bytes);

        String templateString = new String(bytes);

        StringReader reader = new StringReader(templateString);

        VelocityContext ctx = new VelocityContext();
        ctx.put("name", "chase");
        ctx.put("phone", "123123");
        ctx.put("email", "123123@163.com");

        StringWriter out = new StringWriter();
        org.apache.velocity.Template template = new org.apache.velocity.Template();

        RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();
        SimpleNode node = runtimeServices.parse(reader, String.valueOf(template));

        template.setRuntimeServices(runtimeServices);
        template.setData(node);
        template.initDocument();

        template.merge(ctx, out);

        System.out.println(out.toString());
    }

}
