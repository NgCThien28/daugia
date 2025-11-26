package com.example.daugia.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "storage.supabase")
public class SupabaseStorageProperties {
    private String url;           // https://<project>.supabase.co
    private String serviceKey;    // Service Role key (server-only)
    private String bucketProducts;// imgs
    private String cdnBase;       // optional

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getServiceKey() { return serviceKey; }
    public void setServiceKey(String serviceKey) { this.serviceKey = serviceKey; }
    public String getBucketProducts() { return bucketProducts; }
    public void setBucketProducts(String bucketProducts) { this.bucketProducts = bucketProducts; }
    public String getCdnBase() { return cdnBase; }
    public void setCdnBase(String cdnBase) { this.cdnBase = cdnBase; }

    public String storageApiBase() {
        String base = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        return base + "/storage/v1/object";
    }

    public String publicBase() {
        return storageApiBase() + "/public";
    }

    public String buildPublicUrl(String key) {
        // key phải ở dạng "<bucket>/<objectPath>"
        if (cdnBase != null && !cdnBase.isBlank()) {
            String b = cdnBase.endsWith("/") ? cdnBase.substring(0, cdnBase.length() - 1) : cdnBase;
            return b + "/" + key;
        }
        return publicBase() + "/" + key;
    }
}