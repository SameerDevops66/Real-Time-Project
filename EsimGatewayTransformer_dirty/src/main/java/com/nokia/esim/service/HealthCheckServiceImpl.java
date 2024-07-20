package com.nokia.esim.service;

import java.net.InetSocketAddress;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.metadata.Metadata;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class HealthCheckServiceImpl implements HealthCheckService
{

    private static final String OK = "OK";
    private static final String NOK = "NOK";

    private final CommonService commonService;

    @Value("${jaeger.query.health.check.url}")
    private String jaegerQueryHealthCheckUrl;

    @Value("${jaeger.collector.health.check.url}")
    private String jaegerCollectorHealthCheckUrl;

    @Value("${kong1.health.check.url}")
    private String kong1HealthCheckUrl;

    @Value("${kong2.health.check.url}")
    private String kong2HealthCheckUrl;

    @Value("${jaeger.cassandra.host.ip}")
    private String cassandraHost;

    @Value("${jaeger.cassandra.host.port}")
    private String cassandraPort;

    @Value("${jaeger.cassandra.username}")
    private String cassandraUser;

    @Value("${jaeger.cassandra.password}")
    private String cassandraPass;

    @Value("${jaeger.cassandra.datacenter}")
    private String cassandraDatacenter;

    @Value("${jaeger.cassandra.keyspace}")
    private String cassandraKeyspace;

    public HealthCheckServiceImpl(CommonService commonService)
    {
        this.commonService = commonService;
    }

    @Override
    public JSONObject getHealthStatus()
    {
        JSONObject combinedResponse = new JSONObject();
        JSONObject individualResponse = new JSONObject();

     // Check individual component health statuses

        this.checkJaegerCollectorHealth(individualResponse, jaegerCollectorHealthCheckUrl);
                this.checkJaegerQueryHealth(individualResponse, jaegerQueryHealthCheckUrl);
                this.checkCassandraDbHealthMultiNode(individualResponse, cassandraHost, cassandraPort, cassandraUser,
                        cassandraPass, cassandraKeyspace, cassandraDatacenter);
                
                /* Determine Only Kongt2 health and set the overall status accordingly
                  *this.checkKongHealthMultinode(individualResponse, kong1HealthCheckUrl, "kongT1");
                   
               *boolean status = this.checkKongHealthMultinode(individualResponse, kong2HealthCheckUrl, "KongT2");
           
               
                  
        //Include individual component statuses in the response
                  
                 * combinedResponse.put("payload", individualResponse);
                  
               *Set the overall "Status" based on Kong's health
                  *combinedResponse.put("status", status ? "OK" : "NOK");
                 return combinedResponse;*/
                
                
             // Determine both Kong's health and set the overall status accordingly
                boolean kong1Status = this.checkKongHealthMultinode(individualResponse, kong1HealthCheckUrl, "KongT1");
                boolean kong2Status = this.checkKongHealthMultinode(individualResponse, kong2HealthCheckUrl, "KongT2");

                // Include individual component statuses in the response
                combinedResponse.put("payload", individualResponse);

                // Set the overall "Status" based on Kong's health
                if (kong1Status && kong2Status) {
                    combinedResponse.put("status", "OK");
                } else {
                    combinedResponse.put("status", "NOK");
                }
                return combinedResponse;
                  
    }

    private boolean checkJaegerQueryHealth(JSONObject response, String healthCheckUrl)
    {
        try
        {
        	System.out.println("urlpassing"+healthCheckUrl);
            ResponseEntity<String> jaegerQueryStatus = commonService.sendGetRequest(healthCheckUrl, null, String.class);
            if (HttpStatus.OK.equals(jaegerQueryStatus.getStatusCode()))
            {
                response.put("jaegerQuery", OK);
                return true;
            }
        }
        catch (Exception e)
        {
            response.put("jaegerQuery", NOK);
            log.info(CommonServiceImpl.getStackTraceAsString(e));
        }

        return false;
    }

    private boolean checkJaegerCollectorHealth(JSONObject response, String healthCheckUrl)
    {
        try
        {
            ResponseEntity<String> jaegerCollectorStatus = commonService.sendGetRequest(healthCheckUrl, null,
                    String.class);
            if (HttpStatus.OK.equals(jaegerCollectorStatus.getStatusCode()))
            {
                response.put("jaegerCollector", OK);
                return true;
            }
        }
        catch (Exception e)
        {
            response.put("jaegerCollector", NOK);
            log.info(CommonServiceImpl.getStackTraceAsString(e));
        }

        return false;
    }

    /*private boolean checkCassandraDbHealth(JSONObject response, String host, int port, String user, String pass,
       *     String keyspace, String datacenter)
    *{
       * try (CqlSession session = CqlSession.builder().addContactPoint(new InetSocketAddress(host, port))
                .withAuthCredentials(user, pass).withKeyspace(keyspace).withLocalDatacenter(datacenter).build())
        *{
         *   Metadata metadata = session.getMetadata();
          *  log.info("METADATA KEYSPACES :: " + metadata.getKeyspaces());

           * ResultSet resultSet = session.execute("SELECT release_version FROM system.local");
           * String cassandraVersion = resultSet.one().getString("release_version");
           * log.info("Cassandra Release Version :: " + cassandraVersion);

           * response.put("cassandra", OK);
           * return true;
        *}
        * catch (Exception e)
        * {
          *  response.put("cassandra", NOK);
           * log.info(CommonServiceImpl.getStackTraceAsString(e));
        * }

        * return false;
    }*/

    private boolean checkCassandraDbHealthMultiNode(JSONObject response, String hosts, String ports, String users, String passwords,
            String keyspaces, String datacenters)
    {
		String[] hostnames = hosts.split(",");
    	String[] portNumbers = ports.split(",");
    	String[] usernames = users.split(",");
    	String[] passwordArray = passwords.split(",");
        String[] keyspaceArray = keyspaces.split(",");
        String[] datacenterArray = datacenters.split(",");
        
        
        for(int i = 0; i < hostnames.length; i++) {
        	System.out.println("Here I am .. " + (i+1) + " out of " + hostnames.length );
        	System.out.println("Host : " + hostnames[i]);
        	System.out.println("port: " + portNumbers[i]);
        	System.out.println("---------------------------");
        	
        	String cassandraHost = hostnames[i].trim();
        	int cassandraPort = Integer.parseInt(portNumbers[i]);
        	String cassandraUsername = usernames[i].trim();
        	String cassandraPassword = passwordArray[i].trim();
        	String cassandraKeyspace = keyspaceArray[i].trim();
        	String cassandraDatacenter = datacenterArray[i].trim();
        	
        	
        	try {
        		CqlSession session = CqlSession.builder().addContactPoint(
        				new InetSocketAddress(hostnames[i], Integer.parseInt(portNumbers[i])))
                    .withAuthCredentials(usernames[i], passwordArray[i]).withKeyspace(keyspaceArray[i])
                    .withLocalDatacenter(datacenterArray[i]).build();
        		
        		System.out.println("passing the CqlSession");
        		
        		Metadata metadata = session.getMetadata();
                log.info("METADATA KEYSPACES :: " + metadata.getKeyspaces());

				/*
				 * ResultSet resultSet =
				 * session.execute("SELECT release_version FROM system.local"); String
				 * cassandraVersion = resultSet.one().getString("release_version");
				 * log.info("Cassandra Release Version :: " + cassandraVersion);
				 */ 
                 
                System.out.println("");
                response.put("cassandra", OK);
                session.close();  // Memory leak bcz session not closed. 
               if(!session.isClosed()){
            	   session.close();
               }
                return true;
            }
            catch (Exception e)
            {
            	System.out.println("Inside catch, running for db check [" + hostnames[i] + ":" + portNumbers[i] + "]" );
                response.put("cassandra", NOK);
                log.info(CommonServiceImpl.getStackTraceAsString(e));
                if(i != hostnames.length) {
                	continue;
                }
            }
        	
        }

        return false;
    }
    
    
    
    /*private boolean checkKongHealth(JSONObject response, String healthCheckUrl, String kongName)
    *{
        *try
        *{
           * ResponseEntity<String> kongStatus = commonService.sendGetRequest(healthCheckUrl, null, String.class);

            *if (HttpStatus.OK.equals(kongStatus.getStatusCode()))
            *{
             *   response.put(kongName, OK);
              *  return true;
            *}
        *}
        *catch (Exception e)
        *{
            *response.put(kongName, NOK);
            *log.info(CommonServiceImpl.getStackTraceAsString(e));
        *}

        *return false;
   }*/
    
    /*private boolean checkKongHealthMultinode(JSONObject response, String healthCheckUrl, String kongName) {
      *  System.out.println("KongHealth Starts here for " + kongName + ".....1");

       * boolean anyKongUp = false; // Flag to check if any Kong is up

        * if (kongName.equals("kongT1")) {
          *  // For KongT1, use the healthCheckUrl directly without modification
           * try {
            	
            *	System.out.println("Constructed URL for " + kongName + ": " + healthCheckUrl);
             *   ResponseEntity<String> kongStatus = commonService.sendGetRequest(healthCheckUrl, null, String.class);
              *  if (HttpStatus.OK.equals(kongStatus.getStatusCode())) {
               *     response.put(kongName, "OK");
                *    anyKongUp = true;
                *} else {
                 *   response.put(kongName, "NOK");
                *}
           * } catch (Exception e) {
           *     response.put(kongName, "NOK");
            *    log.info(CommonServiceImpl.getStackTraceAsString(e));
            *}
        *} else if (kongName.equals("KongT2")) {
        *	String entireData[] = healthCheckUrl.split("//");
    	*	String http_value=entireData[0];
    	*	String address = entireData[1].split("/")[0];
    	*	System.out.println(http_value);
    	*	System.out.println(address);
    	*	String hostnames[] = address.split(":")[0].split(",");
    	*	String ports[] = address.split(":")[1].split(",");
    		
    	*	// Printing computed URLs
         *   System.out.println("Computed URLs for KongT2:");
          *  for (int i = 0; i < hostnames.length; i++) {
           *     String computedAdre = http_value + "//" + hostnames[i] + ":" + ports[i] + "/status";
            *    System.out.println(computedAdre);

             *   try {
              *      // Constructing and checking Kong status for each split URL
               *     String url = http_value + "//" + hostnames[i] + ":" + ports[i] + "/status";
                *    System.out.println("Constructed URL for KongT2: " + url);
                 *   ResponseEntity<String> kongStatus = commonService.sendGetRequest(url, null, String.class);
                  *  if (HttpStatus.OK.equals(kongStatus.getStatusCode())) {
                   *     response.put(kongName, "OK");
                    *    anyKongUp = true;
                     *   break;
                    *} else {
                     *   response.put(kongName, "NOK");
                    *}
                *} catch (Exception e) {
                 *   response.put(kongName, "NOK");
                  *  log.info(CommonServiceImpl.getStackTraceAsString(e));
                *}
            *}
        *}

        *return anyKongUp;

     *}*/
    
    private boolean checkKongHealthMultinode(JSONObject response, String healthCheckUrl, String kongName) {
    	
            // Splitting the healthCheckUrl to extract hostnames and ports
            String[] entireData = healthCheckUrl.split("//");
            System.out.println("entiredata:"+entireData);
            String httpValue = entireData[0];
            System.out.println("httph:"+httpValue);
            String address = entireData[1].split("/")[0];
            System.out.println("address"+address);
            String[] hostnames = address.split(":")[0].split(",");
            String[] ports = address.split(":")[1].split(",");

            // Printing computed URLs
            System.out.println("Computed URLs for " + kongName + ":");
            
            // Loop through each hostname and port combination
            for (int i = 0; i < hostnames.length; i++) {
                String hostname = hostnames[i].trim();
                String port = ports[i].trim();
                
                // Constructing the URL for this instance
                String url = httpValue + "//" + hostname + ":" + port + "/status";
                System.out.println("Constructed URL for " + kongName + ": " + url);

                // Sending a GET request to check Kong status
                try {
                	
	                ResponseEntity<String> kongStatus = commonService.sendGetRequest(url, null, String.class);
	                System.out.println("Kong1Status:"+kongStatus.getStatusCode() );
	                
	                // Handling the response
	                if (HttpStatus.OK.equals(kongStatus.getStatusCode())) {
	                    // If Kong is up, update response and return true
	                    response.put(kongName, OK);
	                    return true;
	                }
	            }
                catch (Exception e) {
                	log.info(CommonServiceImpl.getStackTraceAsString(e));
                }
            }
        response.put(kongName, NOK);
        return false;	
    }	
    
}




