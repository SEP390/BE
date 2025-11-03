package com.capstone.capstone.constant;

public class ApiConstant {
    public static final String API = "/api";

    public static class USER {
        public static final String USER = API + "/users";
        public static final String PROFILE = "/profile";
        public static final String GET_ALL_RESIDENT = "/residents";
        public static final String GET_RESIDENT_BY_ID = "/residents/{id}";
    }
    public static class AUTH {
        public static final String AUTH = API + "/auth";
    }

    public static class SURVEY {
        public static final String SURVEY = API + "/surveys";
        public static final String GET_BY_ID = "/{id}";
    }

    public static class SURVEY_OPTIONS {
        public static final String SURVEY_OPTIONS = API + "/survey-options";
        public static final String GET_BY_ID = "/{id}";
    }

    public static class SURVEY_SELECT {
        public static final String SURVEY_SELECT = API + "/survey-select";
    }

    public static class REQUEST {
        public static final String REQUEST = API + "/requests";
        public static final String UPDATE = "/{id}";
        public static final String GET_BY_ID = "/{id}";
    }

    public static class EMPLOYEE {
        public static final String EMPLOYEE = API + "/employees";
        public static final String GET_BY_ID = "/{id}";
        public static final String RESET_PASSWORD = "/{id}/passwords";
    }

    public static class NEWS {
        public static final String NEWS = API + "/news";
        public static final String UPDATE = "/{id}";
    }

    public static class REPORTS {
        public static final String REPORT = API + "/reports";
        public static final String GET_BY_ID = "/{id}";
    }

    public static class HOLIDAY {
        public static final String HOLIDAY = API + "/holidays";
        public static final String GET_BY_ID = "/{id}";
    }

    public static class SHIFT {
        public static final String SHIFT = API + "/shifts";
    }
}
