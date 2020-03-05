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
        WsResponse corrResponse = new WsResponse("5b11f4ce-a62d-471e-81fc-a69a8278c7da");
        corrResponse.setName("Nirvana (band)");
        corrResponse.setDescription("<p class=\"mw-empty-elt\">\n</p>\n\n<p class=\"mw-empty-elt\">\n\n</p>\n<p><b>Nirvana</b> was an American rock band formed in Aberdeen, Washington in 1987. It was founded by lead singer and guitarist Kurt Cobain and bassist Krist Novoselic.  Nirvana went through a succession of drummers, the longest-lasting and best-known being Dave Grohl, who joined in 1990. Though the band dissolved in 1994 after the death of Cobain, their music maintains a popular following and continues to influence modern rock and roll culture.\n</p><p>In the late 1980s, Nirvana established itself as part of the Seattle grunge scene, releasing its first album, <i>Bleach</i>, for the independent record label Sub Pop in 1989. They developed a sound that relied on dynamic contrasts, often between quiet verses and loud, heavy choruses. After signing to major label DGC Records in 1991, Nirvana found unexpected mainstream success with \"Smells Like Teen Spirit\", the first single from their landmark second album <i>Nevermind</i> (1991). A cultural phenomenon of the 1990s, the album went on to be certified Diamond by the Recording Industry Association of America (RIAA). Nirvana's sudden success popularized alternative rock, and Cobain found himself described as the \"spokesman of a generation\" and Nirvana the \"flagship band\" of Generation X.</p><p>Following extensive tours and the 1992 compilation album <i>Incesticide</i> and EP <i>Hormoaning</i>, Nirvana released their third studio album, <i>In Utero</i> (1993), to critical acclaim and further chart success. Its abrasive, less mainstream sound challenged the band's audience, and though less successful than <i>Nevermind</i>, it was a commercial success. Nirvana disbanded following the death of Cobain in 1994. Various posthumous releases have been  overseen by Novoselic, Grohl, and Cobain's widow Courtney Love. The posthumous live album <i>MTV Unplugged in New York</i> (1994) won the Grammy Award for Best Alternative Music Album in 1996.\n</p><p>During their three years as a mainstream act, Nirvana was awarded an American Music Award, Brit Award, Grammy Award, seven MTV Video Music Awards and two NME Awards. They have sold over 25 million records in the United States and over 75 million records worldwide, making them one of the best-selling bands of all time. Nirvana has also been ranked as one of the greatest music artists of all time, with <i>Rolling Stone</i> ranking them at number 27 on their list of the 100 Greatest Artists of All Time in 2004, and at number 30 on their updated list in 2011. Nirvana was inducted into the Rock and Roll Hall of Fame in their first year of eligibility in 2014.\n</p>");

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
