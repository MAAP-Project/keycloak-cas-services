package io.github.johnjcool.keycloak.broker.cas.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MaapProfile {
	
    private String public_ssh_key;
    private String id;

    public String getpublic_ssh_key() {
         return public_ssh_key;
    }

    public void setpublic_ssh_key(String value) {
        this.public_ssh_key = value;
    }

    public String getid() {
         return id;
    }

    public void setid(String value) {
        this.id = value;
    }
}
