/*
* Copyright (C) 2013 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

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
