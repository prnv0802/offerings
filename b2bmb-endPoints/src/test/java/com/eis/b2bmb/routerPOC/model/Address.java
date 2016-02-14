package com.eis.b2bmb.routerPOC.model;

import com.eis.core.api.v1.exception.ValidationException;

import java.util.IllegalFormatException;

/**
 * User: mingardia
 * Date: 1/23/14
 * Time: 2:53 PM
 */
public class Address {

    public static class AddressBuilder
    {
        protected String env;
        protected String country;
        protected String region;
        protected String router;
        protected String orgRefName;
        protected String nodeRefName;

        public String getEnv() {
            return env;
        }

        public void setEnv(String env) {
            this.env = env;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getRouter() {
            return router;
        }

        public void setRouter(String router) {
            this.router = router;
        }

        public String getOrgRefName() {
            return orgRefName;
        }

        public void setOrgRefName(String orgRefName) {
            this.orgRefName = orgRefName;
        }

        public String getNodeRefName() {
            return nodeRefName;
        }

        public void setNodeRefName(String nodeRefName) {
            this.nodeRefName = nodeRefName;
        }



        public void validate() throws ValidationException {
            if (this.env == null )
            {
                throw new ValidationException("Environment of the address must be non-null");
            }

            if (this.country == null)
            {
                throw new ValidationException("Country of the address must be non-null");
            }

            if (this.region == null)
            {
                throw new ValidationException("Region of the address must be non-null");
            }

            if (this.router == null)
            {
                throw new ValidationException("Router of the address must be non-null");
            }

            if (this.nodeRefName == null)
            {
                throw new ValidationException("NodeRefName of the address must be non-null");
            }


        }

        public Address build() throws ValidationException {
            validate();
            Address address = new Address();
            address.env = getEnv();
            address.country = getCountry();
            address.region = getRegion();
            address.router = getRouter();
            address.orgRefName = getOrgRefName();
            address.nodeRefName = getNodeRefName();


            return address;
        }
    }


    /**
     * The environment such as DEV, TEST, PROD
     */
    protected String env;

    /**
     * The country, two letter abbrevation should comply with ISO 3166-1 standards
     */
    protected String country;

    /**
     * A numeric value 01, 02, 03
     */
    protected String region;

    /**
     * A numeric value 01, 02, 03
     */
    protected String router;

    /**
     * The organization refName this corresponds to.
     */
    protected String orgRefName;

    /**
     * The node name
     */
    protected String nodeRefName;


    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getRouter() {
        return router;
    }

    public void setRouter(String router) {
        this.router = router;
    }


    public String getOrgRefName() {
        return orgRefName;
    }

    public void setOrgRefName(String orgRefName) {
        this.orgRefName = orgRefName;
    }

    public String getNodeRefName() {
        return nodeRefName;
    }

    public void setNodeRefName(String nodeRefName) {
        this.nodeRefName = nodeRefName;
    }

    @Override
    public String toString() {
        return
                env + '.' +
                country + '.' +
                region + '.' +
                router + '.' +
                orgRefName + '.' +
                nodeRefName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Address address = (Address) o;

        if (country != null ? !country.equals(address.country) : address.country != null) return false;
        if (env != null ? !env.equals(address.env) : address.env != null) return false;
        if (nodeRefName != null ? !nodeRefName.equals(address.nodeRefName) : address.nodeRefName != null) return false;
        if (orgRefName != null ? !orgRefName.equals(address.orgRefName) : address.orgRefName != null) return false;

        if (region != null ? !region.equals(address.region) : address.region != null) return false;
        if (router != null ? !router.equals(address.router) : address.router != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = env != null ? env.hashCode() : 0;
        result = 31 * result + (country != null ? country.hashCode() : 0);
        result = 31 * result + (region != null ? region.hashCode() : 0);
        result = 31 * result + (router != null ? router.hashCode() : 0);
        result = 31 * result + (orgRefName != null ? orgRefName.hashCode() : 0);
        result = 31 * result + (nodeRefName != null ? nodeRefName.hashCode() : 0);
        return result;
    }
}
