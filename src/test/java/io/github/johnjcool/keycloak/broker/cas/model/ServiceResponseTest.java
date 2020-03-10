package io.github.johnjcool.keycloak.broker.cas.model;

import io.github.johnjcool.keycloak.broker.cas.jaxb.ServiceResponseJaxbContextResolver;
import io.github.johnjcool.keycloak.broker.cas.jaxb.ServiceResponseJaxbProvider;
import io.undertow.Undertow;

import java.io.File;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import java.io.*;
import java.nio.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.*;

public class ServiceResponseTest {

	private static UndertowJaxrsServer server;

	@Test
	public void testReadWithAttributes() {
		server.deploy(MyApp.class);
		ResteasyProviderFactory.getInstance().registerProvider(ServiceResponseJaxbProvider.class, true);
		ResteasyProviderFactory.getInstance().registerProvider(ServiceResponseJaxbContextResolver.class, true);
		Client client = ResteasyClientBuilder.newClient(ResteasyProviderFactory.getInstance());
		WebTarget target = client.target(String.format("http://%s:%d%s", "localhost", 9999, "/with-attributes"));
		Response response = target.request().get();
		Assert.assertEquals(200, response.getStatus());
		response.bufferEntity();

		System.out.println(response.readEntity(String.class));

		ServiceResponse serviceResponse = response.readEntity(ServiceResponse.class);
		Success success = serviceResponse.getSuccess();

		Assert.assertEquals("test", success.getUser());
		Assert.assertTrue(!success.getAttributes().isEmpty());
	}

	@Test
	public void testReadWithoutAttributes() {
		server.deploy(MyApp.class);
		ResteasyProviderFactory.getInstance().registerProvider(ServiceResponseJaxbProvider.class, true);
		ResteasyProviderFactory.getInstance().registerProvider(ServiceResponseJaxbContextResolver.class, true);
		Client client = ResteasyClientBuilder.newClient(ResteasyProviderFactory.getInstance());
		WebTarget target = client.target(String.format("http://%s:%d%s", "localhost", 9999, "/without-attributes"));
		Response response = target.request().get();
		Assert.assertEquals(200, response.getStatus());
		response.bufferEntity();

		System.out.println(response.readEntity(String.class));

		ServiceResponse serviceResponse = response.readEntity(ServiceResponse.class);
		Success success = serviceResponse.getSuccess();

		Assert.assertEquals("test", success.getUser());
		Assert.assertTrue(success.getAttributes().isEmpty());
	}

	private static PrivateKey getPK(String filename)
		throws Exception {
		
		java.nio.file.Path localFile = Paths.get(filename);

		byte[] keyBytes = Files.readAllBytes(localFile);

		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePrivate(spec);
	}

	@Test
	public void testReadCertificate() throws Exception {
		//This should be the proxy granting ticket issued by cas upon login
		final String encodedPgt = "";

		final String privateKeyFileLocation = "";
		final PrivateKey privateKey = getPK(privateKeyFileLocation);	
		final Cipher cipher = Cipher.getInstance(privateKey.getAlgorithm());
		final byte[] cred64 = Base64.getDecoder().decode(encodedPgt);
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		final byte[] cipherData = cipher.doFinal(cred64);

		System.out.println(new String(cipherData));
		Assert.assertEquals("test", "test");
	}

	@Path("")
	static public class Resource {

		@GET
		@Path("with-attributes")
		@Consumes("*/*")
		@Produces("text/html; charset=UTF-8")
		public String withAttributes() throws Exception {
			return FileUtils.readFileToString(new File("src/test/resources/test-with-attributes.xml"), "UTF-8");
		}

		@GET
		@Path("without-attributes")
		@Consumes("*/*")
		@Produces("text/html; charset=UTF-8")
		public String withoutAttributes() throws Exception {
			return FileUtils.readFileToString(new File("src/test/resources/test-without-attributes.xml"), "UTF-8");
		}
	}

	@ApplicationPath("")
	public static class MyApp extends Application {
		@Override
		public Set<Class<?>> getClasses() {
			HashSet<Class<?>> classes = new HashSet<Class<?>>();
			classes.add(Resource.class);
			return classes;
		}
	}

	@BeforeClass
	public static void init() throws Exception {
		server = new UndertowJaxrsServer().start(Undertow.builder().addHttpListener(9999, "localhost"));
	}

	@AfterClass
	public static void stop() throws Exception {
		server.stop();
	}
}
