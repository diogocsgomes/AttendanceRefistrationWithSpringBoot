module pt.isec.pd.tp_pd {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.rmi;
    requires com.google.gson;
    requires java.json;

    opens pt.isec.pd.tp_pd to javafx.fxml;
    exports pt.isec.pd.tp_pd;
    exports pt.isec.pd.tp_pd.controllers;
    opens pt.isec.pd.tp_pd.controllers to javafx.fxml;
    exports pt.isec.pd.tp_pd.data;
    opens pt.isec.pd.tp_pd.data to javafx.fxml, com.google.gson;
    exports pt.isec.pd.tp_pd.utils;
    opens pt.isec.pd.tp_pd.utils to javafx.fxml;
}