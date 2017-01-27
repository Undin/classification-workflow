CREATE TABLE IF NOT EXISTS meta_features(
    id                              INTEGER             PRIMARY KEY,
    dataset_name                    TEXT                UNIQUE,
    number_of_instances             DOUBLE PRECISION    NOT NULL,
    number_of_features              DOUBLE PRECISION    NOT NULL,
    number_of_classes               DOUBLE PRECISION    NOT NULL,
    dataset_dimensionality          DOUBLE PRECISION    NOT NULL,
    mean_coefficient_of_variation   DOUBLE PRECISION    NOT NULL,
    mean_kurtosis                   DOUBLE PRECISION    NOT NULL,
    mean_skewness                   DOUBLE PRECISION    NOT NULL,
    mean_standard_deviation         DOUBLE PRECISION    NOT NULL,
    equivalent_number_of_features   DOUBLE PRECISION    NOT NULL,
    max_mutual_information          DOUBLE PRECISION    NOT NULL,
    mean_mutual_information         DOUBLE PRECISION    NOT NULL,
    mean_normalized_feature_entropy DOUBLE PRECISION    NOT NULL,
    noise_signal_ratio              DOUBLE PRECISION    NOT NULL,
    normalized_class_entropy        DOUBLE PRECISION    NOT NULL
);

CREATE TABLE IF NOT EXISTS classifier_performance(
    id                              INTEGER             PRIMARY KEY,
    classifier_name                 TEXT                NOT NULL,
    dataset_name                    TEXT                NOT NULL,
    measure                         DOUBLE PRECISION    NOT NULL,
    unique (dataset_name, classifier_name)
);

CREATE TABLE IF NOT EXISTS transformer_performance(
    id                              INTEGER             PRIMARY KEY,
    transformer_name                TEXT                NOT NULL,
    classifier_name                 TEXT                NOT NULL,
    dataset_name                    TEXT                NOT NULL,
    measure                         DOUBLE PRECISION    NOT NULL,
    unique (dataset_name, transformer_name, classifier_name)
);

CREATE SEQUENCE IF NOT EXISTS meta_features_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 0
    NO MAXVALUE
    OWNED BY meta_features.id;

CREATE SEQUENCE IF NOT EXISTS classifier_performance_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 0
    NO MAXVALUE
    OWNED BY classifier_performance.id;

CREATE SEQUENCE IF NOT EXISTS transformer_performance_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 0
    NO MAXVALUE
    OWNED BY transformer_performance.id;
