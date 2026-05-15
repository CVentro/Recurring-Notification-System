package com.cventro.recurringService.service;

import com.cventro.recurringService.configuration.RetryConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.appconfigdata.AppConfigDataClient;
import software.amazon.awssdk.services.appconfigdata.model.GetLatestConfigurationRequest;
import software.amazon.awssdk.services.appconfigdata.model.GetLatestConfigurationResponse;
import software.amazon.awssdk.services.appconfigdata.model.StartConfigurationSessionRequest;
import software.amazon.awssdk.services.appconfigdata.model.StartConfigurationSessionResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static org.assertj.core.api.Assertions.assertThat;

class AwsAppConfigServiceTest {

    private final FakeAppConfigDataClient fakeAppConfigDataClient = new FakeAppConfigDataClient();
    private final AppConfigDataClient appConfigDataClient = fakeAppConfigDataClient.client();
    private final AwsAppConfigService appConfigService = new AwsAppConfigService(
            appConfigDataClient,
            new ObjectMapper()
    );

    @Test
    void fetchesRetryConfigFromAppConfig() {
        fakeAppConfigDataClient.addConfigurationResponse("token-2", "{\"retryCount\":5,\"retryInterval\":10000}");

        RetryConfig retryConfig = appConfigService.getRetryConfig();

        assertThat(retryConfig.retryCount()).isEqualTo(5);
        assertThat(retryConfig.retryInterval()).isEqualTo(10000);
        assertThat(fakeAppConfigDataClient.startSessionRequests).hasSize(1);
        assertThat(fakeAppConfigDataClient.getLatestConfigurationRequests).hasSize(1);
    }

    @Test
    void keepsExistingConfigWhenAppConfigHasNoUpdate() {
        fakeAppConfigDataClient.addConfigurationResponse("token-2", "{\"retryCount\":5,\"retryInterval\":10000}");
        fakeAppConfigDataClient.addConfigurationResponse("token-3", "");

        appConfigService.getRetryConfig();
        RetryConfig retryConfig = appConfigService.getRetryConfig();

        assertThat(retryConfig.retryCount()).isEqualTo(5);
        assertThat(retryConfig.retryInterval()).isEqualTo(10000);
    }

    private static class FakeAppConfigDataClient implements InvocationHandler {

        private final List<StartConfigurationSessionRequest> startSessionRequests = new ArrayList<>();
        private final List<GetLatestConfigurationRequest> getLatestConfigurationRequests = new ArrayList<>();
        private final Queue<GetLatestConfigurationResponse> configurationResponses = new ArrayDeque<>();

        private AppConfigDataClient client() {
            return (AppConfigDataClient) Proxy.newProxyInstance(
                    AppConfigDataClient.class.getClassLoader(),
                    new Class[]{AppConfigDataClient.class},
                    this
            );
        }

        private void addConfigurationResponse(String token, String configuration) {
            configurationResponses.add(GetLatestConfigurationResponse.builder()
                    .nextPollConfigurationToken(token)
                    .configuration(SdkBytes.fromUtf8String(configuration))
                    .build());
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            if ("startConfigurationSession".equals(method.getName())) {
                startSessionRequests.add((StartConfigurationSessionRequest) args[0]);
                return StartConfigurationSessionResponse.builder()
                        .initialConfigurationToken("token-1")
                        .build();
            }

            if ("getLatestConfiguration".equals(method.getName())) {
                getLatestConfigurationRequests.add((GetLatestConfigurationRequest) args[0]);
                return configurationResponses.remove();
            }

            if ("close".equals(method.getName())) {
                return null;
            }

            if ("toString".equals(method.getName())) {
                return "FakeAppConfigDataClient";
            }

            if ("hashCode".equals(method.getName())) {
                return System.identityHashCode(proxy);
            }

            if ("equals".equals(method.getName())) {
                return proxy == args[0];
            }

            throw new UnsupportedOperationException("Unsupported method: " + method.getName());
        }
    }
}
