<?php
/**
 * Extension MobileFrontend — Mobile Frontend
 *
 * @file
 * @ingroup Extensions
 * @author Patrick Reilly
 * @copyright © 2011 Patrick Reilly
 * @licence GNU General Public Licence 2.0 or later
 */

// Needs to be called within MediaWiki; not standalone
if ( !defined( 'MEDIAWIKI' ) ) {
	echo( "This is an extension to the MediaWiki package and cannot be run standalone.\n" );
	die( -1 );
}

// Define the extension; allows us make sure the extension is used correctly
define( 'MOBILEFRONTEND', 'MobileFrontend' );

// Extension credits that will show up on Special:Version
$wgExtensionCredits['other'][] = array(
	'path' => __FILE__,
	'name' => 'MobileFrontend',
	'version' => '0.7.0',
	'author' => array( 'Patrick Reilly', 'Max Semenik', 'Jon Robson', 'Arthur Richards' ),
	'descriptionmsg' => 'mobile-frontend-desc',
	'url' => 'https://www.mediawiki.org/wiki/Extension:MobileFrontend',
);

$cwd = dirname( __FILE__ );
$wgExtensionMessagesFiles['MobileFrontend'] = "$cwd/MobileFrontend.i18n.php";
$wgExtensionMessagesFiles['MobileFrontendAlias'] = "$cwd/MobileFrontend.alias.php";

// autoload extension classes

$autoloadClasses = array (
	'ExtMobileFrontend' => 'MobileFrontend.body',

	'DeviceDetection' => 'DeviceDetection',
	'HtmlFormatter' => 'HtmlFormatter',
	'MobileFormatter' => 'MobileFormatter',
	'WmlContext' => 'WmlContext',

	'ApiMobileView' => 'api/ApiMobileView',
	'ApiParseExtender' => 'api/ApiParseExtender',
	'ApiQueryExtracts' => 'api/ApiQueryExtracts',

	'SpecialMobileFeedback' => 'specials/SpecialMobileFeedback',
	'SpecialMobileOptions' => 'specials/SpecialMobileOptions',

	'MobileFrontendTemplate' => 'templates/MobileFrontendTemplate',
	'ApplicationTemplate' => 'templates/ApplicationTemplate',
	'SearchTemplate'  => 'templates/SearchTemplate',
	'FooterTemplate' => 'templates/FooterTemplate',
	'ApplicationWmlTemplate' => 'templates/ApplicationWmlTemplate',
	'SopaNoticeTemplate' => 'templates/SopaNoticeTemplate',

	'SkinMobile' => 'skins/SkinMobile',
	'SkinMobileTemplate' => 'skins/SkinMobileTemplate',
	'SkinMobileBase' => 'skins/SkinMobileBase',
	'SkinMobileWML' => 'skins/SkinMobileWML',
	'SkinMobileTemplateWML' => 'skins/SkinMobileTemplateWML',

	'MFCompatCheck' => 'MFCompatCheck',
);

foreach ( $autoloadClasses as $className => $classFilename ) {
	if ( !isset( $wgAutoloadClasses[$className] ) ) {
		$wgAutoloadClasses[$className] = "$cwd/$classFilename.php";
	}
}

/**
 * Path to the logo used in the mobile view
 *
 * Should be 22px tall at most
 */
$wgMobileFrontendLogo = false;

/**
 * Template for mobile URLs.
 *
 * This will be used to transcode regular URLs into mobile URLs for the
 * mobile view.
 *
 * It's possible to specify the 'mobileness' of the URL in the host portion of
 * the URL.
 *
 * You can either statically or dynamically create the host-portion of your
 * mobile URL. To statically create it, just set $wgMobileUrlTemplate to
 * the static hostname. For example:
 *		$wgMobileUrlTemplate = "mobile.mydomain.com";
 *
 * Alternatively, the host definition can include placeholders for different
 * parts of the 'host' section of a URL. The placeholders are denoted by '%h'
 * and followed with a digit that maps to the position of a host-part of the
 * original, non-mobile URL. Take the host 'en.wikipedia.org' for example.
 * '%h0' maps to 'en', '%h1' maps to 'wikipedia', and '%h2' maps to 'org'.
 * So, if you wanted a mobile URL scheme that turned "en.wikipedia.org" into
 * "en.m.wikipedia.org", your URL template would look like:
 * 		%h0.m.%h1.%h2
 */
$wgMobileUrlTemplate = '';

/**
 * The number of seconds the 'useformat' cookie should be valid
 *
 * The useformat cookie gets set when a user manually elects to view
 * either the mobile or desktop view of the site.
 *
 * If this value is not set, it will default to $wgCookieExpiration
 */
$wgMobileFrontendFormatCookieExpiry = null;

/**
 * URL for script used to disable mobile site
 * (protocol, host, optional port; path portion)
 *
 * e.g., http://en.wikipedia.org/w/mobileRedirect.php
 */
$wgMobileRedirectFormAction = false;

/**
 * A string to mark the particular version of a Javascript or CSS resource
 *
 * This is useful to update in order to force invalidation of certain caches
 * when new versions of this software gets deployed, as this string gets
 * appended to the query string in the request for resources, which will
 * invalidate caches dependent on URLs.
 *
 * This is entirely optional.
 */
$wgMobileResourceVersion = '';

/**
 * When set to true, the feedback form will post to a remote wiki, which
 * must also be configured.
 * @param bool
 */
$wgMFRemotePostFeedback = false;
$wgMFRemotePostFeedbackUrl;
$wgMFRemotePostFeedbackUsername;
$wgMFRemotePostFeedbackPassword;
$wgMFRemotePostFeedbackArticle;

/**
 * Configure the href links for the various links that appear on the
 * MobileFrontend feedback form.
 *
 * These can be any value that you can use as an href value in <a href="">,
 * eg "GeneralFeedback", "http://mysite.com/wiki/GeneralFeedback",
 *   "mailto:someone@example.com"
 *
 * Leaving a value empty will default to a value of '#'
 *
 * Alternatively, you can invoke the 'MobileFrontendOverrideFeedbackLinks' hook
 * rather than just set this var in your LocalSettings. This is really useful
 * if you have more complicated/variable needs for setting up this configuration
 * var that you might not want running on every single page load.
 */
$wgMFFeedbackLinks = array(
	'General' => '', // General feedback
	'ArticlePersonal' => '', // Regarding me, a person, or a company I work for
	'ArticleFactual' => '', // Regarding a factual error
	'ArticleOther' => '', // Regarding another problem
);

$wgExtMobileFrontend = null;

/**
 * A fallback URL for a 'contact us' page if one cannot be dynamically
 * determined for the project (using wfMsg( 'Contact-us' )). This is only
 * used in non-beta mode.
 */
$wgMFFeedbackFallbackURL = '#';

/**
 * Set properties in ExtMobileFrontend to arbitrary values
 * CAUTION: this should not be used in production environments
 *
 * This array can consist of key => value pairs, mapping to
 * '<property_name>' => <property_value>
 * Any properties you try to set that do not exist in ExtMobileFrontend will
 * be ignored.
 */
$wgMFConfigProperties = array();

$wgExtensionFunctions[] = 'efMobileFrontend_Setup';

if ( version_compare( $wgVersion, '1.20alpha', '<' ) ) {
	$wgAPIPropModules['extracts'] = 'ApiQueryExtracts';
}
$wgAPIModules['mobileview'] = 'ApiMobileView';

$wgHooks['APIGetAllowedParams'][] = 'ApiParseExtender::onAPIGetAllowedParams';
$wgHooks['APIAfterExecute'][] = 'ApiParseExtender::onAPIAfterExecute';
$wgHooks['APIGetParamDescription'][] = 'ApiParseExtender::onAPIGetParamDescription';
$wgHooks['APIGetDescription'][] = 'ApiParseExtender::onAPIGetDescription';
$wgHooks['OpenSearchXml'][] = 'ApiQueryExtracts::onOpenSearchXml';

$wgSpecialPages['MobileFeedback'] = 'SpecialMobileFeedback';
$wgSpecialPages['MobileOptions'] = 'SpecialMobileOptions';

function efMobileFrontend_Setup() {
	global $wgExtMobileFrontend, $wgHooks;
	$wgExtMobileFrontend = new ExtMobileFrontend( RequestContext::getMain() );
	$wgExtMobileFrontend->attachHooks();
}

// Unit tests
$wgHooks['UnitTestsList'][] = 'efExtMobileFrontendUnitTests';

/**
 * @param $files array
 * @return bool
 */
function efExtMobileFrontendUnitTests( &$files ) {
	$dir = dirname( __FILE__ ) . '/tests';
	$files[] = "$dir/MobileFrontendTest.php";
	$files[] = "$dir/DeviceDetectionTest.php";
	$files[] = "$dir/HtmlFormatterTest.php";
	$files[] = "$dir/MobileFormatterTest.php";
	return true;
}

// ResourceLoader modules, so far CSS-only
$wgResourceModules['ext.mobileFrontend'] = array(
	'styles' => array( 'stylesheets/common.css', 'stylesheets/footer.css',
		'stylesheets/contact-us.css', 'stylesheets/banner.css',
		'stylesheets/header.css', 'stylesheets/sections.css',
		'stylesheets/references.css', 'stylesheets/hacks.css' ),
	'localBasePath' => dirname( __FILE__ ),
	'remoteExtPath' => 'MobileFrontend',
);
$wgResourceModules['ext.mobileFrontendBeta'] = $wgResourceModules['ext.mobileFrontend'];
$wgResourceModules['ext.mobileFrontend.filePage'] = array(
	'styles' => array( 'stylesheets/filepage.css' ),
	'localBasePath' => dirname( __FILE__ ),
	'remoteExtPath' => 'MobileFrontend',
);

/**
 * Begin configuration variables
 */

/**
 * Make the classes, tags and ids stripped from page content configurable.
 * Each item will be stripped from the page.
 * See $itemsToRemove for more information.
 */
$wgMFRemovableClasses = array();

/**
 * Make the logos configurable.
 * Key for site.
 * Key for logo.
 * Example: array('site' => 'mysite', 'logo' => 'mysite_logo.png');
 */
$wgMFCustomLogos = array();

/**
 * Set to false if you need to debug JavaScript.
 */
$wgMFMinifyJS = true;

/**
 * Whether this extension should provide its extracts to OpenSearchXml extension
 */
$wgMFExtendOpenSearchXml = false;

/**
 * Set to false to allow search engines to index your mobile pages. So far, Google seems
 * to mix mobile and non-mobile pages in its search results, creating confusion.
 */
$wgMFNoindexPages = true;
