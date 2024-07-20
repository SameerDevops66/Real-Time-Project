package com.nokia.esim.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@PropertySource("file:${external.properties.path}")
//@PropertySource("file:./eSimGwTransformer.properties")
public class TransformerConfig
{

//    private static final double SAMPLING_RATE = 1;
//    private static final String JAEGER_COLLECTOR_HOST = "10.76.117.86";
//    private static final int JAEGER_COLLECTOR_PORT = 14268;

    @Bean
    public RestTemplate restTemplate()
    {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);

        return new RestTemplate(factory);
    }

//    @Bean
//    public io.opentracing.Tracer remoteJaegerTracer()
//    {
//        io.jaegertracing.Configuration.SamplerConfiguration samplerConfig = io.jaegertracing.Configuration.SamplerConfiguration
//                .fromEnv().withType(ProbabilisticSampler.TYPE).withParam(SAMPLING_RATE);
//
//        io.jaegertracing.Configuration.ReporterConfiguration reporterConfig = io.jaegertracing.Configuration.ReporterConfiguration
//                .fromEnv().withLogSpans(true)
//                .withSender(io.jaegertracing.Configuration.SenderConfiguration.fromEnv()
//                        .withAgentHost(JAEGER_COLLECTOR_HOST).withAgentPort(JAEGER_COLLECTOR_PORT)
//                        .withEndpoint("http://" + JAEGER_COLLECTOR_HOST + ":" + JAEGER_COLLECTOR_PORT + "/api/traces"));
//
//        io.jaegertracing.Configuration config = new io.jaegertracing.Configuration("Transformer")
//                .withSampler(samplerConfig).withReporter(reporterConfig);
//
//        return config.getTracer();
//    }

}
