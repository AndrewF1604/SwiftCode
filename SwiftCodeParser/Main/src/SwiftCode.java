public class SwiftCode {
    private String countryISO2;
    private String swiftCode;
    private String bankName;
    private String address;
    private String countryName;
    private boolean isHeadquarter;

    public SwiftCode(String countryISO2, String swiftCode, String bankName, String address, 
                     String countryName, boolean isHeadquarter) {
        this.countryISO2 = countryISO2;
        this.swiftCode = swiftCode;
        this.bankName = bankName;
        this.address = address;
        this.countryName = countryName;
        this.isHeadquarter = isHeadquarter;
    }

    public String getCountryISO2() {
        return countryISO2;
    }

    public String getSwiftCode() {
        return swiftCode;
    }

    public String getBankName() {
        return bankName;
    }

    public String getAddress() {
        return address;
    }

    public String getCountryName() {
        return countryName;
    }

    public boolean isHeadquarter() {
        return isHeadquarter;
    }

    public void setCountryISO2(String countryISO2) {
        this.countryISO2 = countryISO2;
    }

    public void setSwiftCode(String swiftCode) {
        this.swiftCode = swiftCode;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public void setHeadquarter(boolean isHeadquarter) {
        this.isHeadquarter = isHeadquarter;
    }
}
