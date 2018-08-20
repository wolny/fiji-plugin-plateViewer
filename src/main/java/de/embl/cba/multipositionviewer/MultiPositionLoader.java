package de.embl.cba.multipositionviewer;

import ij.IJ;
import ij.ImagePlus;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.SingleCellArrayImg;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiPositionLoader implements CellLoader
{
	final int[] cellDimensions;
	final int bitDepth;
	final Map< String, File > cellFileMap;
	final int numIoThreads;
	final ExecutorService executorService;

	public MultiPositionLoader( int[] cellDimensions, int bitDepth, Map< String, File > cellFileMap, int numIoThreads )
	{
		this.cellDimensions = cellDimensions;
		this.bitDepth = bitDepth;
		this.cellFileMap = cellFileMap;
		this.numIoThreads = numIoThreads;
		executorService = Executors.newFixedThreadPool( numIoThreads );
	}


	@Override
	public void load( final SingleCellArrayImg cell ) throws Exception
	{
		File file = getFile( cell );

		if ( file != null )
		{
			executorService.submit( new Runnable()
			{
				@Override
				public void run()
				{
					loadImageIntoCell( cell, file );
				}
			});
		}

	}

	public File getFile( SingleCellArrayImg cell )
	{
		final int[] position = new int[ 2 ];

		for ( int d = 0; d < 2; ++d )
		{
			position[ d ] = (int) ( cell.min( d ) / cell.dimension( d ) );
		}

		String key = Utils.getCellString( position );

		File file;

		if ( cellFileMap.containsKey( key ) )
		{
			file = cellFileMap.get( key );
		}
		else
		{
			file = null;
		}
		return file;
	}


	private void loadImageIntoCell( SingleCellArrayImg cell, File file )
	{
		Utils.debug( "Loading: " + file.getName() );

		// TODO: check for the data type of the loaded cell (cell.getFirstElement())

		final ImagePlus imp = IJ.openImage( file.getAbsolutePath() );

		if ( bitDepth == 8 )
		{
			final byte[] impdata = ( byte[] ) imp.getProcessor().getPixels();
			final byte[] celldata = ( byte[] ) cell.getStorageArray();
			System.arraycopy( impdata, 0, celldata, 0, celldata.length );
		}
		else if ( bitDepth == 16 )
		{
			final short[] impdata = ( short[] ) imp.getProcessor().getPixels();
			final short[] celldata = ( short[] ) cell.getStorageArray();
			System.arraycopy( impdata, 0, celldata, 0, celldata.length );
		}
		else if ( bitDepth == 32 )
		{
			final float[] impdata = ( float[] ) imp.getProcessor().getPixels();
			final float[] celldata = ( float[] ) cell.getStorageArray();
			System.arraycopy( impdata, 0, celldata, 0, celldata.length );
		}
	}

}