package com.example.mashup;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import static io.restassured.RestAssured.baseURI;
import static org.hamcrest.Matchers.notNullValue;

import static io.restassured.RestAssured.get;
import static org.hamcrest.CoreMatchers.equalTo;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
class MashupWebServiceApplicationTests {

    @LocalServerPort
    int randomServerPort;

    @Test
    void contextLoads() {
    }

    private String uri;

    @PostConstruct
    public void init() {
        uri = "http://localhost:8080";
    }

    //@MockBean
   // MashupWS appService;

    /**
     * Test to ensure that WS is working and retrives correct data
     *
     * @throws UnsupportedEncodingException
     **/

    @Test
    public void mbidFoundThroughWS() throws UnsupportedEncodingException, URISyntaxException, ParseException {
        RestTemplate restTemplate = new RestTemplate();
        final String baseUrl = "http://localhost:" + randomServerPort + "/mashupws?mbid=5b11f4ce-a62d-471e-81fc-a69a8278c7da";
        URI uri = new URI(baseUrl);
        ResponseEntity<String> result = restTemplate.getForEntity(uri,String.class);

        JSONParser parse = new JSONParser();
        JSONObject jobj = (JSONObject) parse.parse(result.getBody().toString());
        JSONArray albums = (JSONArray) jobj.get("albums");

        Assert.assertEquals(200, result.getStatusCodeValue());
        Assert.assertEquals(true, albums.size() == 25);
        Assert.assertEquals(true, jobj.get("name").equals("Nirvana (band)"));
        Assert.assertEquals(true, jobj.get("mbid").equals("5b11f4ce-a62d-471e-81fc-a69a8278c7da"));
    }

}
