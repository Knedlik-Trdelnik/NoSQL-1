package alfarius.yushinon.nosql1.dto;


public enum Roles {
    Applicant("Applicant"),
    Administrator("Administrator");

    private final String value;

    Roles(String value) {
        this.value = value;
    }
}
