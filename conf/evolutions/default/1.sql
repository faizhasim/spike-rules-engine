# Users schema

# --- !Ups

CREATE TABLE checkoutitem (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    customerId bigint(255) NOT NULL,
    productId bigint(255) NOT NULL,
    PRIMARY KEY (id)
);
ALTER TABLE checkoutitem ADD UNIQUE idx_unique_customer_product(customerId, productId);
ALTER TABLE checkoutitem ADD INDEX idx_customer_id (customerId);

# --- !Downs

DROP TABLE checkoutitem;