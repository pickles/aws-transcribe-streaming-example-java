package com.amazonaws.transcribestreaming;

import java.util.concurrent.CompletableFuture;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
import software.amazon.awssdk.services.translate.TranslateAsyncClient;
import software.amazon.awssdk.services.translate.model.TranslateTextRequest;
import software.amazon.awssdk.services.translate.model.TranslateTextResponse;

public class TranslateClient {
  private TranslateAsyncClient client;

  public TranslateClient(AwsCredentialsProvider credentials)
  {
    this.client = TranslateAsyncClient.builder()
      .credentialsProvider(credentials)
      .region(getRegion())
      .build();
  }

  private static Region getRegion() {
    Region region;
    try {
      region = new DefaultAwsRegionProviderChain().getRegion();
    } catch (SdkClientException e) {
      System.out.println("Could not get region. Using default as us-west-2");
      region = Region.US_WEST_2;
    }
    return region;
  }
  
  public CompletableFuture<TranslateTextResponse> translate(
    String text,
    String sourceLanguageCode,
    String targetLanguageCode
  ) {
    TranslateTextRequest request = TranslateTextRequest.builder()
      .sourceLanguageCode(sourceLanguageCode)
      .targetLanguageCode(targetLanguageCode)
      .text(text)
      .build();

    return client.translateText(request);
  }
}
