module Mosaic {
	requires transitive javafx.base;
    requires transitive javafx.graphics;
    requires transitive javafx.controls;
    requires transitive javafx.web;
	requires javafx.fxml;
	requires java.sql;
	requires java.prefs;

	requires jackson.annotations;
	requires jackson.databind;
	requires jackson.core;
    requires java.desktop;

    opens ai.cogmission.mosaic to javafx.graphics;

    exports ai.cogmission.mosaic;
}