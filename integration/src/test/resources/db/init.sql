INSERT INTO product_category(category_id, name, is_visible, parent_category_id) VALUE (1, "clothes", true, null);

INSERT INTO product(product_id, uuid, name, description, default_image_key, weight, weight_type, category_id)
    VALUE (1, "product-uuid", "product-1", "lorem 5000", "image-key", 2.5, "kg", 1);