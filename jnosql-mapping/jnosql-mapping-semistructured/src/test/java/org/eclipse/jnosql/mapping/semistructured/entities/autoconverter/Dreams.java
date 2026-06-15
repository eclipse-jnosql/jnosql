package org.eclipse.jnosql.mapping.semistructured.entities.autoconverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Dreams {

    private final List<String> wishes = new ArrayList<>();

     public List<String> getWishes() {
        return Collections.unmodifiableList(wishes);
    }

    @Override
    public String toString() {
        return String.join(",", this.wishes);
    }

    public static Dreams parse(String value) {
        Dreams dreams = new Dreams();
        dreams.wishes.addAll(List.of(value.split(",")));
        return dreams;
    }

     public void addWish(String wish) {
        this.wishes.add(wish);
    }
}
