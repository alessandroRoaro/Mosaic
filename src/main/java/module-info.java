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

    opens ai.cogmission.mosaic.refimpl.javafx to javafx.graphics;
//	opens apro2.canbustool to javafx.graphics;
//    opens apro2.canbustool.ui.controller to javafx.graphics, javafx.fxml;
//    opens apro2.canbustool.ui.view to javafx.graphics, javafx.fxml;
//    opens apro2.canbustool.test to javafx.graphics;
//    opens apro2.canbustool.model to javafx.base;
}