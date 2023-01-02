package tistory;

import lombok.Data;

@Data
public class TistoryCategory {
    private String id;
    private String name;
    private String parent;
    private String label;
    private String entries;
    private String entriesInLogin;
}
