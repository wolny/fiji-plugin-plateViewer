package de.embl.cba.multipositionviewer;

import net.imglib2.FinalInterval;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageSourcesGeneratorMDMultiSite
{
	final ArrayList< File > files;

	int numSites;
	int[] siteDimensions;
	int[] wellDimensions;
	int[] imageDimensions;

	final ArrayList< ImageSource > imageSources;

	final static String namingScheme = Utils.PATTERN_MD_A01_S1_CHANNEL;

	public ImageSourcesGeneratorMDMultiSite( ArrayList< File > files, int[] imageDimensions )
	{
		this.files = files;
		this.imageDimensions = imageDimensions;

		this.imageSources = new ArrayList<>();
		setImageSources();

	}

	public ArrayList< ImageSource > getFileList()
	{
		return imageSources;
	}

	private void setImageSources()
	{
		configWells( files );
		configSites( files );

		for ( File file : files )
		{
			final ImageSource imageSource = new ImageSource(
					file,
					getInterval( file ),
					file.getName());

			imageSources.add( imageSource );
		}
	}

	private void configWells( ArrayList< File > files )
	{
		int[] maximalWellPositionsInData = getMaximalWellPositionsInData( files );

		wellDimensions = Utils.guessWellDimensions( maximalWellPositionsInData );

		Utils.log( "Well dimensions [ 0 ] : " +  wellDimensions[ 0 ] );
		Utils.log( "Well dimensions [ 1 ] : " +  wellDimensions[ 1 ] );
	}

	private void configSites( ArrayList< File > files )
	{
		numSites = getNumSites( files );
		siteDimensions = new int[ 2 ];
		siteDimensions[ 0 ] = (int) Math.sqrt( numSites );
		siteDimensions[ 1 ] = (int) Math.sqrt( numSites );

		Utils.log( "Site dimensions [ 0 ] : " +  siteDimensions[ 0 ] );
		Utils.log( "Site dimensions [ 1 ] : " +  siteDimensions[ 1 ] );
	}

	private int getNumSites( ArrayList< File > files )
	{
		Set< String > sites = new HashSet<>( );

		for ( File file : files )
		{
			final String pattern = Utils.getNamingScheme( file );

			final Matcher matcher = Pattern.compile( pattern ).matcher( file.getName() );

			if ( matcher.matches() )
			{
				sites.add( matcher.group( 2 ) );
			}
		}

		if ( sites.size() == 0 )
		{
			return 1;
		}
		else
		{
			return sites.size();
		}

	}

	private int[] getMaximalWellPositionsInData( ArrayList< File > files )
	{
		int[] maximalWellPosition = new int[ 2 ];

		for ( File file : files )
		{
			final Matcher matcher = Pattern.compile( namingScheme ).matcher( file.getName() );

			matcher.matches();

			int[] wellPosition = getWellPositionFromA01( matcher.group( 1 ) );

			for ( int d = 0; d < wellPosition.length; ++d )
			{
				if ( wellPosition[ d ] > maximalWellPosition[ d ] )
				{
					maximalWellPosition[ d ] = wellPosition[ d ];
				}
			}
		}

		return maximalWellPosition;

	}


	private FinalInterval getInterval( File file )
	{
		String filePath = file.getAbsolutePath();

		final Matcher matcher = Pattern.compile( namingScheme ).matcher( filePath );

		if ( matcher.matches() )
		{
			int[] wellPosition = getWellPositionFromA01( matcher.group( 1 ) );
			int[] sitePosition = getSitePositionFromSiteIndex( matcher.group( 2 ) );

			final FinalInterval interval = Utils.createInterval( wellPosition, sitePosition, siteDimensions, imageDimensions );

			return interval;

		}
		else
		{
			return null;
		}

	}

	private int[] getSitePositionFromSiteIndex( String site )
	{
		int[] sitePosition = new int[ 2 ];
		int siteIndex = Integer.parseInt( site ) - 1;

		sitePosition[ 0 ] = siteIndex % siteDimensions[ 1 ];
		sitePosition[ 1 ] = siteIndex / siteDimensions[ 1 ];

		return sitePosition;
	}

	private int[] getWellPositionFromA01( String well )
	{
		int[] wellPosition = new int[ 2 ];
		wellPosition[ 0 ] = Integer.parseInt( well.substring( 1, 3 ) ) - 1;
		wellPosition[ 1 ] = Utils.CAPITAL_ALPHABET.indexOf( well.substring( 0, 1 ) );
		return wellPosition;
	}


}