package com.nokia.esim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EsimGatewayTransformerApplication
{

	public static void main(String[] args)
	{
		System.out.println("Starting...");
		SpringApplication.run(EsimGatewayTransformerApplication.class, args);
		System.out.println("Started...");
	}

}
