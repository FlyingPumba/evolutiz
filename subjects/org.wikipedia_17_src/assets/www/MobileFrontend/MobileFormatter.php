<?php

/**
 * Converts HTML into a mobile-friendly version
 */
class MobileFormatter extends HtmlFormatter {
	const WML_SECTION_SEPARATOR = '***************************************************************************';

	protected $format;

	/**
	 * @var Title
	 */
	protected $title;

	protected $expandableSections = false;
	protected $mainPage = false;

	private $headings = 0;

	/**
	 * @var WmlContext
	 */
	protected $wmlContext;

	private static $defaultItemsToRemove = array(
		'#contentSub',
		'div.messagebox',
		'#siteNotice',
		'#siteSub',
		'#jump-to-nav',
		'div.editsection',
		'div.infobox',
		'table.toc',
		'#catlinks',
		'div.stub',
		'form',
		'div.sister-project',
		'script',
		'div.magnify',
		'.editsection',
		'span.t',
		'sup[style*="help"]',
		'.portal',
		'#protected-icon',
		'.printfooter',
		'.boilerplate',
		'#id-articulo-destacado',
		'#coordinates',
		'#top',
		'.hiddenStructure',
		'.noprint',
		'.medialist',
		'.mw-search-createlink',
		'#ogg_player_1',
		'#ogg_player_2',
		'.nomobile',
	);

	/**
	 * Constructor
	 *
	 * @param string $html: Text to process
	 * @param Title $title: Title to which $html belongs
	 * @param string $format: 'HTML' or 'WML'
	 * @param WmlContext $wmlContext: Context for creation of WML cards, can be omitted if $format == 'HTML'
	 * @throws MWException
	 */
	public function __construct( $html, $title, $format, WmlContext $wmlContext = null ) {
		parent::__construct( $html );

		$this->setHtmlMode( true ); // Our current mobile skins always output HTML
		$this->title = $title;
		$this->format = $format;
		if ( !$wmlContext && $format == 'WML' ) {
			throw new MWException( __METHOD__ . '(): WML context not set' );
		}
		$this->wmlContext = $wmlContext;
		$this->flattenRedLinks();
	}

	/**
	 * @return string: Output format
	 */
	public function getFormat() {
		return $this->format;
	}

	/**
	 * @todo: kill with fire when there will be minimum of pre-1.1 app users remaining
	 * @param bool $flag 
	 */
	public function enableExpandableSections( $flag = true ) {
		$this->expandableSections = $flag;
	}

	public function setIsMainPage( $value = true ) {
		$this->mainPage = $value;
	}

	/**
	 * Removes content inappropriate for mobile devices
	 * @param bool $removeDefaults: Whether default settings at self::$defaultItemsToRemove should be used
	 */
	public function filterContent( $removeDefaults = true ) {
		global $wgMFRemovableClasses;

		if ( $removeDefaults ) {
			$this->remove( self::$defaultItemsToRemove );
			$this->remove( $wgMFRemovableClasses );
		}
		parent::filterContent();
	}

	/**
	 * Performs final transformations to mobile format and returns resulting HTML/WML
	 *
	 * @param DOMElement|string|null $element: ID of element to get HTML from or false to get it from the whole tree
	 * @return string: Processed HTML
	 */
	public function getText( $element = null ) {
		wfProfileIn( __METHOD__ );
		if ( $this->mainPage ) {
			$element = $this->parseMainPage( $this->getDoc() );
		}
		$html = parent::getText( $element );
		wfProfileOut( __METHOD__ );
		return $html;
	}

	protected function onHtmlReady( $html ) {
		switch ( $this->format ) {
			case 'HTML':
				if ( $this->expandableSections && !$this->mainPage && strlen( $html ) > 4000 ) {
					$html = $this->headingTransform( $html );
				}
				break;
			case 'WML':
				$html = $this->headingTransform( $html );
				// Content removal for WML rendering
				$this->flatten( array( 'span', 'div', 'sup', 'h[1-6]', 'sup', 'sub' ) );
				// Content wrapping
				$html = $this->createWMLCard( $html );
				break;
		}
		return $html;
	}

	/**
	 * Callback for headingTransform()
	 * @param array $matches
	 * @return string
	 */
	private function headingTransformCallbackWML( $matches ) {
		wfProfileIn( __METHOD__ );
		$this->headings++;

		$base = self::WML_SECTION_SEPARATOR .
				"<h2 class='section_heading' id='section_{$this->headings}'>{$matches[2]}</h2>";

		wfProfileOut( __METHOD__ );
		return $base;
	}

	/**
	 * Callback for headingTransform()
	 * @param array $matches
	 * @return string
	 */
	private function headingTransformCallbackHTML( $matches ) {
		wfProfileIn( __METHOD__ );
		if ( isset( $matches[0] ) ) {
			preg_match( '/id="([^"]*)"/', $matches[0], $headlineMatches );
		}

		$headlineId = ( isset( $headlineMatches[1] ) ) ? $headlineMatches[1] : '';

		$show = $this->msg( 'mobile-frontend-show-button' );
		$hide = $this->msg( 'mobile-frontend-hide-button' );
		$backToTop = $this->msg( 'mobile-frontend-back-to-top-of-section' );
		$this->headings++;
		// Back to top link
		$backToTop = Html::openElement( 'div',
				array( 'id' => 'anchor_' . intval( $this->headings - 1 ),
					'class' => 'section_anchors',
				)
			)
			. Html::rawElement( 'a',
				array( 'href' => '#section_' . intval( $this->headings - 1 ),
						'class' => 'back_to_top'
				),
				'&#8593;' . $backToTop
			)
			. '</div>'; // <div id="anchor_*">

		// generate the HTML we are going to inject
		// TODO: remove legacy code for Wikipedia Mobile app < 1.3 which is not using the api
		// when usage of said apps is low
		$buttons = Html::element( 'button',
				array( 'class' => 'section_heading show', 'section_id' => $this->headings ),
				$show
			)
			. Html::element( 'button',
				array( 'class' => 'section_heading hide', 'section_id' => $this->headings ),
				$hide
			);
		$base = Html::openElement( 'div', array( 'class' => 'section' ) );
		if ( $this->expandableSections ) {
			$h2OnClick = 'javascript:wm_toggle_section(' . $this->headings . ');';
			$base .= Html::openElement( 'h2',
				array( 'class' => 'section_heading',
					'id' => 'section_' . $this->headings, 'onclick' => $h2OnClick
				)
			);
		} else {
			$base .= Html::openElement( 'h2',
				array( 'class' => 'section_heading', 'id' => 'section_' . $this->headings )
			);
		}
		$base .= $buttons .
			Html::rawElement( 'span',
					array( 'id' => $headlineId ),
					$matches[2]
				)
				. Html::closeElement( 'h2' )
				. Html::openElement( 'div',
					array( 'class' => 'content_block', 'id' => 'content_' . $this->headings )
				);

		if ( $this->headings > 1 ) {
			// Close it up here
			$base = '</div>' // <div class="content_block">
				. $backToTop
				. "</div>" // <div class="section">
				. $base;
		}

		wfProfileOut( __METHOD__ );
		return $base;
	}

	/**
	 * Creates a WML card from input
	 * @param string $s: Raw WML
	 * @return string: WML card
	 */
	protected function createWMLCard( $s ) {
		wfProfileIn( __METHOD__ );
		$segments = explode( self::WML_SECTION_SEPARATOR, $s );
		$card = '';
		$idx = 0;
		$requestedSegment = htmlspecialchars( $this->wmlContext->getRequestedSegment() );
		$title = htmlspecialchars( $this->title->getText() );
		$segmentText = $this->wmlContext->getOnlyThisSegment()
			? str_replace( self::WML_SECTION_SEPARATOR, '', $s )
			: $segments[$requestedSegment];

		$card .= "<card id='s{$idx}' title='{$title}'><p>{$segmentText}</p>";
		$idx = $requestedSegment + 1;
		$segmentsCount = $this->wmlContext->getOnlyThisSegment()
			? $idx + 1 // @todo: when using from API we don't have the total section count
			: count( $segments );
		$card .= "<p>" . $idx . "/" . $segmentsCount . "</p>";

		$useFormatParam = ( $this->wmlContext->getUseFormat() )
			? '&amp;useformat=' . $this->wmlContext->getUseFormat()
			: '';

		// Title::getLocalUrl doesn't work at this point since PHP 5.1.x, all objects have their destructors called
		// before the output buffer callback function executes.
		// Thus, globalized objects will not be available as expected in the function.
		// This is stated to be intended behavior, as per the following: [http://bugs.php.net/bug.php?id=40104]
		$defaultQuery = wfCgiToArray( preg_replace( '/^.*?(\?|$)/', '', $this->wmlContext->getCurrentUrl() ) );
		unset( $defaultQuery['seg'] );
		unset( $defaultQuery['useformat'] );

		$qs = wfArrayToCGI( $defaultQuery );
		$delimiter = ( !empty( $qs ) ) ? '?' : '';
		$basePageParts = wfParseUrl( $this->wmlContext->getCurrentUrl() );
		$basePage = $basePageParts['scheme'] . $basePageParts['delimiter'] . $basePageParts['host'] . $basePageParts['path'] . $delimiter . $qs;
		$appendDelimiter = ( $delimiter === '?' ) ? '&amp;' : '?';

		if ( $idx < $segmentsCount ) {
			$card .= "<p><a href=\"{$basePage}{$appendDelimiter}seg={$idx}{$useFormatParam}\">"
				. $this->msg( 'mobile-frontend-wml-continue' ) . "</a></p>";
		}

		if ( $idx > 1 ) {
			$back_idx = $requestedSegment - 1;
			$card .= "<p><a href=\"{$basePage}{$appendDelimiter}seg={$back_idx}{$useFormatParam}\">"
				. $this->msg( 'mobile-frontend-wml-back' ) . "</a></p>";
		}

		$card .= '</card>';
		wfProfileOut( __METHOD__ );
		return $card;
	}

	/**
	 * Prepares headings in WML mode, makes sections expandable in HTML mode
	 * @param string $s
	 * @return string
	 */
	protected function headingTransform( $s ) {
		wfProfileIn( __METHOD__ );
		$callback = "headingTransformCallback{$this->format}";

		// Closures are a PHP 5.3 feature.
		// MediaWiki currently requires PHP 5.2.3 or higher.
		// So, using old style for now.
		$s = preg_replace_callback(
			'%<h2(.*)<span class="mw-headline" [^>]*>(.+)</span>[\s\r\n]*</h2>%sU',
			array( $this, $callback ),
			$s
		);

		// if we had any, make sure to close the whole thing!
		if ( $this->headings > 0 ) {
			$s .= '</div>' // <div class="content_block">
				. "\n</div>"; // <div class="section">
		}
		wfProfileOut( __METHOD__ );
		return $s;
	}

	/**
	 * Returns interface message text
	 * @param string $key: Message key
	 * @return string
	 */
	protected function msg( $key ) {
		return wfMsg( $key );
	}

	/**
	 * Performs transformations specific to main page
	 * @param DOMDocument $mainPage: Tree to process
	 * @return DOMElement
	 */
	protected function parseMainPage( DOMDocument $mainPage ) {
		wfProfileIn( __METHOD__ );

		$zeroLandingPage = $mainPage->getElementById( 'zero-landing-page' );
		$featuredArticle = $mainPage->getElementById( 'mp-tfa' );
		$newsItems = $mainPage->getElementById( 'mp-itn' );

		$xpath = new DOMXpath( $mainPage );
		$elements = $xpath->query( '//*[starts-with(@id, "mf-")]' );

		$commonAttributes = array( 'mp-tfa', 'mp-itn' );

		$content = $mainPage->createElement( 'div' );
		$content->setAttribute( 'id', 'mainpage' );

		if ( $zeroLandingPage ) {
			$content->appendChild( $zeroLandingPage );
		}

		if ( $featuredArticle ) {
			$h2FeaturedArticle = $mainPage->createElement( 'h2', $this->msg( 'mobile-frontend-featured-article' ) );
			$content->appendChild( $h2FeaturedArticle );
			$content->appendChild( $featuredArticle );
		}

		if ( $newsItems ) {
			$h2NewsItems = $mainPage->createElement( 'h2', $this->msg( 'mobile-frontend-news-items' ) );
			$content->appendChild( $h2NewsItems );
			$content->appendChild( $newsItems );
		}

		foreach ( $elements as $element ) {
			if ( $element->hasAttribute( 'id' ) ) {
				$id = $element->getAttribute( 'id' );
				if ( !in_array( $id, $commonAttributes ) ) {
					$elementTitle = $element->hasAttribute( 'title' ) ? $element->getAttribute( 'title' ) : '';
					$h2UnknownMobileSection = $mainPage->createElement( 'h2', $elementTitle );
					$br = $mainPage->createElement( 'br' );
					$br->setAttribute( 'clear', 'all' );
					$content->appendChild( $h2UnknownMobileSection );
					$content->appendChild( $element );
					$content->appendChild( $br );
				}
			}
		}

		wfProfileOut( __METHOD__ );
		return $content;
	}
}
