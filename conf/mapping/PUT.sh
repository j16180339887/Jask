curl -XPUT 'localhost:9200/jask?pretty' -d'
{
    "mappings": {
        "questions": {
            "_all": {
                "enabled": false
            },
            "properties": {
                "created": {
                    "format": "strict_date_optional_time||epoch_millis",
                    "type": "date"
                },
                "title": {
                    "type": "string"
                },
                "body": {
                    "type": "string"
                },
                "user_id": {
                    "type": "string"
                },
                "tags": {
                    "type": "string"
                },
                "score": {
                    "type": "long"
                },
                "has_best_answer": {
                    "type": "boolean"
                },
                "comments": {
                    "properties": {
                        "created": {
                            "format": "strict_date_optional_time||epoch_millis",
                            "type": "date"
                        },
                        "edited": {
                            "format": "strict_date_optional_time||epoch_millis",
                            "type": "date"
                        },
                        "body": {
                            "type": "string"
                        },
                        "user_id": {
                            "type": "string"
                        }
                    }
                },
                "answers": {
                    "properties": {
                        "created": {
                            "format": "strict_date_optional_time||epoch_millis",
                            "type": "date"
                        },
                        "edited": {
                            "format": "strict_date_optional_time||epoch_millis",
                            "type": "date"
                        },
                        "body": {
                            "type": "string"
                        },
                        "user_id": {
                            "type": "string"
                        },
                        "score": {
                            "type": "long"
                        },
                        "is_best_answer": {
                            "type": "boolean"
                        },
                        "comments": {
                            "properties": {
                                "created": {
                                    "format": "strict_date_optional_time||epoch_millis",
                                    "type": "date"
                                },
                                "edited": {
                                    "format": "strict_date_optional_time||epoch_millis",
                                    "type": "date"
                                },
                                "body": {
                                    "type": "string"
                                },
                                "user_id": {
                                    "type": "string"
                                }
                            }
                        }
                    }
                }
            }
        },
        "user": {
            "_all": {
                "enabled": false
            },
            "properties": {
                "score": {
                    "type": "long"
                },
                "user_id": {
                    "type": "string"
                },
                "password": {
                    "type": "string"
                }
            }
        }
    }
}'
