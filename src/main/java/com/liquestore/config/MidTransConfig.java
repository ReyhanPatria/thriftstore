package com.liquestore.config;

import com.midtrans.Config;
import com.midtrans.service.MidtransCoreApi;
import com.midtrans.service.MidtransIrisApi;
import com.midtrans.service.MidtransSnapApi;
import com.midtrans.service.impl.MidtransCoreApiImpl;
import com.midtrans.service.impl.MidtransIrisApiImpl;
import com.midtrans.service.impl.MidtransSnapApiImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MidTransConfig {
    @Value("${midtrans.server-key}")
    private String serverKey;

    @Value("${midtrans.client-key}")
    private String clientKey;

    @Bean
    public MidtransSnapApi midtransSnapApi() {
        Config config = Config.builder()
                .setServerKey(serverKey)
                .setClientKey(clientKey)
                .setIsProduction(false)
                .build();
        return new MidtransSnapApiImpl(config);
    }

    @Bean
    public MidtransCoreApi midtransCoreApi() {
        Config config = Config.builder()
                .setServerKey(serverKey)
                .setClientKey(clientKey)
                .setIsProduction(false)
                .build();
        return new MidtransCoreApiImpl(config);
    }

    @Bean
    public MidtransIrisApi midtransIrisApi() {
        Config config = Config.builder()
                .setServerKey(serverKey)
                .setClientKey(clientKey)
                .setIsProduction(false)
                .build();
        return new MidtransIrisApiImpl(config);
    }
}
