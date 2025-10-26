CREATE TABLE addresses (
      uuid UUID PRIMARY KEY,
      user_id UUID NOT NULL,
      street VARCHAR(255) NOT NULL,
      building_name VARCHAR(255) NOT NULL,
      city VARCHAR(100) NOT NULL,
      state VARCHAR(100) NOT NULL,
      country VARCHAR(50) NOT NULL,
      pin_code VARCHAR(20) NOT NULL,
      created_at TIMESTAMP NOT NULL,
      updated_at TIMESTAMP,
      CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(uuid) ON DELETE CASCADE
  );