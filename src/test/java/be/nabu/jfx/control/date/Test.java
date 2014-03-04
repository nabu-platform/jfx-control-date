package be.nabu.jfx.control.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Test extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		BorderPane root = new BorderPane();
		VBox vbox = new VBox();
		final DatePicker picker = new DatePicker();
//		picker.setHideTimeControls(true);
		picker.formatProperty().setValue("yyyy/MM/dd HH:mm:ss.SSS");
		picker.filterProperty().setValue(new DateFilter() {
			@Override
			public boolean accept(Date date) {
				SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
				try {
					return date.after(parser.parse("2010-07-13"));
				}
				catch (ParseException e) {
					return false;
				}
			}
		});
		vbox.getChildren().add(picker);
		vbox.getChildren().add(new Button("test"));
		root.setCenter(vbox);
		Button button = new Button("Show");
		root.setBottom(button);
		Scene scene = new Scene(root, 500, 500);
		primaryStage.setTitle("Test");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String...args) {
		launch(args);
	}
}
