package org.cobbzilla.util.http;

import lombok.*;

import static org.cobbzilla.util.reflect.ReflectionUtil.copy;

@NoArgsConstructor @AllArgsConstructor @ToString(of={"baseUri", "user"})
public class ApiConnectionInfo {

    @Getter @Setter private String baseUri;
    @Getter @Setter private String user;
    @Getter @Setter private String password;

    public ApiConnectionInfo (String baseUri) { this.baseUri = baseUri; }

    public ApiConnectionInfo (ApiConnectionInfo other) { copy(this, other); }

    // alias for when this is used in json with snake_case naming conventions
    public String getBase_uri () { return getBaseUri(); }
    public void setBase_uri (String uri) { setBaseUri(uri); }

}
