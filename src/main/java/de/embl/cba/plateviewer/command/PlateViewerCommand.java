package de.embl.cba.plateviewer.command;

import de.embl.cba.plateviewer.view.PlateViewerImageView;
import de.embl.cba.plateviewer.table.DefaultImageNameTableRow;
import de.embl.cba.tables.select.DefaultSelectionModel;
import de.embl.cba.tables.view.TableRowsTableView;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.awt.*;
import java.io.File;
import java.util.List;

import static de.embl.cba.plateviewer.table.ImageNameTableRows.imageNameTableRowsFromFilePath;

@Plugin(type = Command.class, menuPath = "Plugins>Screening>PlateViewer..." )
public class PlateViewerCommand implements Command
{
	@Parameter (label = "Plate images directory", style = "directory" )
	public File imagesDirectory;

	@Parameter (label = "Only load image files matching" )
	public String filePattern = ".*.tif";

	@Parameter (label = "Plate images table (optional)", required = false)
	public File imagesTableFile;

	public void run()
	{
		final PlateViewerImageView imagePlateView = new PlateViewerImageView( imagesDirectory.toString(), filePattern, 1 );

		if ( imagesTableFile != null )
		{
			final DefaultSelectionModel< DefaultImageNameTableRow > selectionModel = new DefaultSelectionModel<>();

			final List< DefaultImageNameTableRow > tableRows = imageNameTableRowsFromFilePath(
					imagesTableFile.getAbsolutePath(),
					imagePlateView.getImageNamingScheme() );

			final TableRowsTableView< DefaultImageNameTableRow > imageTableView = new TableRowsTableView<>( tableRows, selectionModel );
			final Component parentComponent = imagePlateView.getBdv().getBdvHandle().getViewerPanel();
			imageTableView.showTableAndMenu( parentComponent );

			imagePlateView.installImageSelectionModel( tableRows, selectionModel );
		}
	}
}

