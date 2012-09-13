package net.robotmedia.billing.utils;

/**
 * Used to provide on-demand values to the billing controller.
 */
public interface IConfiguration {

    /**
     * Returns a salt for the obfuscation of purchases in local memory.
     *
     * @return array of 20 random bytes.
     */
    public byte[] getObfuscationSalt();

    /**
     * Returns the public key used to verify the signature of responses of
     * the Market Billing service.
     *
     * @return Base64 encoded public key.
     */
    public String getPublicKey();
}
