# Checkout schema

# --- !Ups


CREATE TABLE checkoutitem (
    id BIGINT(20) PRIMARY KEY NOT NULL AUTO_INCREMENT,
    customerId VARCHAR(32) NOT NULL,
    productId VARCHAR(32) NOT NULL
);
CREATE INDEX idx_customer_id ON checkoutitem (customerId);

CREATE TABLE pricingrules (
    customerId VARCHAR(32) PRIMARY KEY NOT NULL,
    classicProductRule VARCHAR(255),
    standoutProductRule VARCHAR(255),
    premiumProductRule VARCHAR(255)
);

INSERT INTO pricingrules
  (customerId, classicProductRule, standoutProductRule, premiumProductRule)
VALUES
  ('_', '{"productPrice":"269.99","strategy":"FlatRule"}', '{"productPrice":"322.99","strategy":"FlatRule"}', '{"productPrice":"394.99","strategy":"FlatRule"}'),
  ('UNILEVER', '{"pay":2,"for":3,"strategy":"PayXForYRule"}', null, null),
  ('APPLE', null, '{"productPrice":"299.99","strategy":"FlatRule"}', null),
  ('NIKE', null, null, '{"productUnitAmount":4,"productPrice":"379.99","strategy":"EqualsOrMorePurchasedRule"}'),
  ('FORD', '{"pay":5,"for":4,"strategy":"PayXForYRule"}', '{"productPrice":"309.99","strategy":"FlatRule"}', '{"productUnitAmount":3,"productPrice":"389.99","strategy":"EqualsOrMorePurchasedRule"}');

# --- !Downs

DROP TABLE checkoutitem;
DROP TABLE pricingrules;