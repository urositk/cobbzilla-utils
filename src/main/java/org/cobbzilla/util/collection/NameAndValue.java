package org.cobbzilla.util.collection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.Accessors;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

@NoArgsConstructor @AllArgsConstructor @Accessors(chain=true) @ToString
public class NameAndValue {

    @Getter @Setter private String name;
    public boolean hasName () { return !empty(name); }
    @JsonIgnore public boolean getHasName () { return !empty(name); }

    @Getter @Setter private String value;
    public boolean hasValue () { return !empty(value); }
    @JsonIgnore public boolean getHasValue () { return !empty(value); }

}
