package io.github.johnjcool.keycloak.broker.cas.util;

import io.github.johnjcool.keycloak.broker.cas.CasIdentityProviderConfig;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import io.github.johnjcool.keycloak.broker.cas.model.*;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.broker.provider.BrokeredIdentityContext;


public class MaapHelper {
	private static final String MAAP_PROFILE_ENDPOINT = "/api/members/self";

	private MaapHelper() {
		// util
	}

	public static MaapProfile getMaapProfile(final CasIdentityProviderConfig config, final String proxyGrantingTicket) {
    	
    	MaapProfile maapProfile = getMaapProfile(config.getMaapApiServerUrl(), proxyGrantingTicket);
    	
    	return maapProfile;
	}

	public static MaapProfile getMaapProfile(final String maapApiServerUrl, final String proxyGrantingTicket) {
		
		try {
	    	Client client = ResteasyClientBuilder.newClient();
	    	
	    	WebTarget target = client.target(maapApiServerUrl + MAAP_PROFILE_ENDPOINT);    	
	    	Response response = target
	    			.request()
	      			.header("proxy-ticket", proxyGrantingTicket)
	    			.accept("application/json")
	    			.get();
	    	
	    	MaapProfile maapProfile = response.readEntity(MaapProfile.class);

	    	response.close();
	    	client.close();
	    	
	    	return maapProfile;
		} catch (Exception e) {
			return null;
		}
	}

}
