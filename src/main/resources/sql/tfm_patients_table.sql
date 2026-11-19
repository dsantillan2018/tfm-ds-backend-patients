-- Table to store patient information
CREATE TABLE IF NOT EXISTS tfm_patients (
    patient_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    document_number VARCHAR(20) NOT NULL UNIQUE,
    document_type VARCHAR(20) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    birth_date DATE NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    gender VARCHAR(20),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE'
);

-- Table to store the relationship between patients and dentists
CREATE TABLE IF NOT EXISTS tfm_patients_dentists (
    patient_dentist_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES tfm_patients(patient_id) ON DELETE CASCADE,
    dentist_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    -- Ensure that each patient can only be associated with each dentist once
    CONSTRAINT unique_patient_dentist UNIQUE (patient_id, dentist_id)
);

-- Create an index on the patient_id column for faster lookups
CREATE INDEX idx_patients_dentist_dentist_id
    ON tfm_patients_dentists (dentist_id);
