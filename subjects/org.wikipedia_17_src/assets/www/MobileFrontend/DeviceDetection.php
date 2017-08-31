<?php
/**
 * Extension MobileFrontend — Device Detection
 *
 * @file
 * @ingroup Extensions
 * @author Patrick Reilly
 * @copyright © 2011 Patrick Reilly
 * @licence GNU General Public Licence 2.0 or later
 */

// Provides an abstraction for a device
// A device can select which format a request should recieve and
// may be extended to provide access to particular devices functionality
class DeviceDetection {

	/**
	 * @return array
	 */
	public function getAvailableFormats() {
		$formats = array (
			'html' => array (
				'view_format' => 'html',
				'search_bar' => 'default',
				'footmenu' => 'default',
				'with_layout' => 'application',
				'css_file_name' => 'default',
				'supports_javascript' => false,
				'supports_jquery' => false,
				'disable_zoom' => true,
				'parser' => 'html',
				'disable_links' => true,
			),
			'capable' => array (
				'view_format' => 'html',
				'search_bar' => 'default',
				'footmenu' => 'default',
				'with_layout' => 'application',
				'css_file_name' => 'default',
				'supports_javascript' => true,
				'supports_jquery' => true,
				'disable_zoom' => true,
				'parser' => 'html',
				'disable_links' => true,
			),
			'webkit' => array (
				'view_format' => 'html',
				'search_bar' => 'webkit',
				'footmenu' => 'default',
				'with_layout' => 'application',
				'css_file_name' => 'webkit',
				'supports_javascript' => true,
				'supports_jquery' => true,
				'disable_zoom' => false,
				'parser' => 'html',
				'disable_links' => true,
			),
			'ie' => array (
				'view_format' => 'html',
				'search_bar' => 'default',
				'footmenu' => 'default',
				'with_layout' => 'application',
				'css_file_name' => 'default',
				'supports_javascript' => true,
				'supports_jquery' => true,
				'disable_zoom' => false,
				'parser' => 'html',
				'disable_links' => true,
			),
			'android' => array (
				'view_format' => 'html',
				'search_bar' => 'default',
				'footmenu' => 'default',
				'with_layout' => 'application',
				'css_file_name' => 'android',
				'supports_javascript' => true,
				'supports_jquery' => true,
				'disable_zoom' => false,
				'parser' => 'html',
				'disable_links' => true,
			),
			'iphone' => array (
				'view_format' => 'html',
				'search_bar' => 'webkit',
				'footmenu' => 'default',
				'with_layout' => 'application',
				'css_file_name' => 'iphone',
				'supports_javascript' => true,
				'supports_jquery' => true,
				'disable_zoom' => false,
				'parser' => 'html',
				'disable_links' => true,
			),
			'iphone2' => array (
				'view_format' => 'html',
				'search_bar' => 'default',
				'footmenu' => 'default',
				'with_layout' => 'application',
				'css_file_name' => 'iphone2',
				'supports_javascript' => true,
				'supports_jquery' => true,
				'disable_zoom' => true,
				'parser' => 'html',
				'disable_links' => true,
			),
			'native_iphone' => array (
				'view_format' => 'html',
				'search_bar' => false,
				'footmenu' => 'default',
				'with_layout' => 'application',
				'css_file_name' => 'default',
				'supports_javascript' => true,
				'supports_jquery' => true,
				'disable_zoom' => false,
				'parser' => 'html',
				'disable_links' => false,
			),
			'palm_pre' => array (
				'view_format' => 'html',
				'search_bar' => 'default',
				'footmenu' => 'default',
				'with_layout' => 'application',
				'css_file_name' => 'palm_pre',
				'supports_javascript' => true,
				'supports_jquery' => false,
				'disable_zoom' => true,
				'parser' => 'html',
				'disable_links' => true,
			),
			'kindle' => array (
				'view_format' => 'html',
				'search_bar' => 'kindle',
				'footmenu' => 'default',
				'with_layout' => 'application',
				'css_file_name' => 'kindle',
				'supports_javascript' => false,
				'supports_jquery' => false,
				'disable_zoom' => true,
				'parser' => 'html',
				'disable_links' => true,
			),
			'kindle2' => array (
				'view_format' => 'html',
				'search_bar' => 'kindle',
				'footmenu' => 'default',
				'with_layout' => 'application',
				'css_file_name' => 'kindle',
				'supports_javascript' => false,
				'supports_jquery' => false,
				'disable_zoom' => true,
				'parser' => 'html',
				'disable_links' => true,
			),
			'blackberry' => array (
				'view_format' => 'html',
				'search_bar' => 'default',
				'footmenu' => 'default',
				'with_layout' => 'application',
				'css_file_name' => 'blackberry',
				'supports_javascript' => true,
				'supports_jquery' => false,
				'disable_zoom' => true,
				'parser' => 'html',
				'disable_links' => true,
			),
			'blackberry-lt5' => array (
				'view_format' => 'html',
				'search_bar' => 'default',
				'footmenu' => 'default',
				'with_layout' => 'application',
				'css_file_name' => 'blackberry',
				'supports_javascript' => false,
				'supports_jquery' => false,
				'disable_zoom' => true,
				'parser' => 'html',
				'disable_links' => true,
			),
			'netfront' => array (
				'view_format' => 'html',
				'search_bar' => 'simple',
				'footmenu' => 'simple',
				'with_layout' => 'application',
				'css_file_name' => 'simple',
				'supports_javascript' => false,
				'supports_jquery' => false,
				'disable_zoom' => true,
				'parser' => 'html',
				'disable_links' => true,
			),
			'wap2' => array (
				'view_format' => 'html',
				'search_bar' => 'simple',
				'footmenu' => 'simple',
				'with_layout' => 'application',
				'css_file_name' => 'simple',
				'supports_javascript' => false,
				'supports_jquery' => false,
				'disable_zoom' => true,
				'parser' => 'html',
				'disable_links' => true,
			),
			'psp' => array (
				'view_format' => 'html',
				'search_bar' => 'simple',
				'footmenu' => 'simple',
				'with_layout' => 'application',
				'css_file_name' => 'psp',
				'supports_javascript' => false,
				'supports_jquery' => false,
				'disable_zoom' => true,
				'parser' => 'html',
				'disable_links' => true,
			),
			'ps3' => array (
				'view_format' => 'html',
				'search_bar' => 'simple',
				'footmenu' => 'simple',
				'with_layout' => 'application',
				'css_file_name' => 'simple',
				'supports_javascript' => false,
				'supports_jquery' => false,
				'disable_zoom' => true,
				'parser' => 'html',
				'disable_links' => true,
			),
			'wii' => array (
				'view_format' => 'html',
				'search_bar' => 'wii',
				'footmenu' => 'default',
				'with_layout' => 'application',
				'css_file_name' => 'wii',
				'supports_javascript' => true,
				'supports_jquery' => true,
				'disable_zoom' => true,
				'parser' => 'html',
				'disable_links' => true,
			),
			'operamini' => array (
				'view_format' => 'html',
				'search_bar' => 'simple',
				'footmenu' => 'simple',
				'with_layout' => 'application',
				'css_file_name' => 'operamini',
				'supports_javascript' => false,
				'supports_jquery' => false,
				'disable_zoom' => true,
				'parser' => 'html',
				'disable_links' => true,
			),
			'operamobile' => array (
				'view_format' => 'html',
				'search_bar' => 'simple',
				'footmenu' => 'simple',
				'with_layout' => 'application',
				'css_file_name' => 'operamobile',
				'supports_javascript' => true,
				'supports_jquery' => true,
				'disable_zoom' => true,
				'parser' => 'html',
				'disable_links' => true,
			),
			'nokia' => array (
				'view_format' => 'html',
				'search_bar' => 'webkit',
				'footmenu' => 'default',
				'with_layout' => 'application',
				'css_file_name' => 'nokia',
				'supports_javascript' => true,
				'supports_jquery' => false,
				'disable_zoom' => true,
				'parser' => 'html',
				'disable_links' => true,
			),
			'wml' => array (
				'view_format' => 'wml',
				'search_bar' => 'wml',
				'supports_javascript' => false,
				'supports_jquery' => false,
				'parser' => 'wml',
			),
		);
		return $formats;
	}

	/**
	 * @param $userAgent
	 * @param string $acceptHeader
	 * @return array
	 */
	public function detectDevice( $userAgent, $acceptHeader = '' ) {
		$formatName = $this->detectFormatName( $userAgent, $acceptHeader );
		return $this->getDevice( $formatName );
	}

	/**
	 * @param $formatName
	 * @return array
	 */
	public function getDevice( $formatName ) {
		$format = $this->getAvailableFormats();
		return ( isset( $format[$formatName] ) ) ? $format[$formatName] : array();
	}

	/**
	 * @param $userAgent string
	 * @param $acceptHeader string
	 * @return string
	 */
	public function detectFormatName( $userAgent, $acceptHeader = '' ) {
		$formatName = '';

		if ( preg_match( '/Android/', $userAgent ) ) {
			$formatName = 'android';
			if ( strpos( $userAgent, 'Opera Mini' ) !== false ) {
				$formatName = 'operamini';
			}
		} else if ( preg_match( '/MSIE 9.0/', $userAgent ) ||
				preg_match( '/MSIE 8.0/', $userAgent ) ) {
			$formatName = 'ie';
		} else if( preg_match( '/MSIE/', $userAgent ) ) {
			$formatName = 'html';
		} else if ( strpos( $userAgent, 'Opera Mobi' ) !== false ) {
			$formatName = 'operamobile';
		} elseif ( preg_match( '/iPad.* Safari/', $userAgent ) ) {
			$formatName = 'iphone';
		} elseif ( preg_match( '/iPhone.* Safari/', $userAgent ) ) {
			if ( strpos( $userAgent, 'iPhone OS 2' ) !== false ) {
				$formatName = 'iphone2';
			} else {
				$formatName = 'iphone';
			}
		} elseif ( preg_match( '/iPhone/', $userAgent ) ) {
			if ( strpos( $userAgent, 'Opera' ) !== false ) {
				$formatName = 'operamini';
			} else {
				$formatName = 'native_iphone';
			}
		} elseif ( preg_match( '/WebKit/', $userAgent ) ) {
			if ( preg_match( '/Series60/', $userAgent ) ) {
				$formatName = 'nokia';
			} elseif ( preg_match( '/webOS/', $userAgent ) ) {
				$formatName = 'palm_pre';
			} else {
				$formatName = 'webkit';
			}
		} elseif ( preg_match( '/Opera/', $userAgent ) ) {
			if ( strpos( $userAgent, 'Nintendo Wii' ) !== false ) {
				$formatName = 'wii';
			} elseif ( strpos( $userAgent, 'Opera Mini' ) !== false ) {
				$formatName = 'operamini';
			} elseif ( strpos( $userAgent, 'Opera Mobi' ) !== false ) {
				$formatName = 'iphone';
			} else {
				$formatName = 'webkit';
			}
		} elseif ( preg_match( '/Kindle\/1.0/', $userAgent ) ) {
			$formatName = 'kindle';
		} elseif ( preg_match( '/Kindle\/2.0/', $userAgent ) ) {
			$formatName = 'kindle2';
		} elseif ( preg_match( '/Firefox/', $userAgent ) ) {
			$formatName = 'capable';
		} elseif ( preg_match( '/NetFront/', $userAgent ) ) {
			$formatName = 'netfront';
		} elseif ( preg_match( '/SEMC-Browser/', $userAgent ) ) {
			$formatName = 'wap2';
		} elseif ( preg_match( '/Series60/', $userAgent ) ) {
			$formatName = 'wap2';
		} elseif ( preg_match( '/PlayStation Portable/', $userAgent ) ) {
			$formatName = 'psp';
		} elseif ( preg_match( '/PLAYSTATION 3/', $userAgent ) ) {
			$formatName = 'ps3';
		} elseif ( preg_match( '/SAMSUNG/', $userAgent ) ) {
			$formatName = 'capable';
		} elseif ( preg_match( '/BlackBerry/', $userAgent ) ) {
			if( preg_match( '/BlackBerry[^\/]*\/[1-4]\./', $userAgent ) ) {
				$formatName = 'blackberry-lt5';
			} else {
				$formatName = 'blackberry';
			}
		}

		if ( $formatName === '' ) {
			if ( strpos( $acceptHeader, 'application/vnd.wap.xhtml+xml' ) !== false ) {
				// Should be wap2
				$formatName = 'html';
			} elseif ( strpos( $acceptHeader, 'vnd.wap.wml' ) !== false ) {
				$formatName = 'wml';
			} else {
				$formatName = 'html';
			}
		}
		return $formatName;
	}

	/**
	 * @return array: List of all device-specific stylesheets
	 */
	public function getCssFiles() {
		$devices = $this->getAvailableFormats();
		$files = array();
		foreach ( $devices as $dev ) {
			if ( isset( $dev['css_file_name'] ) ) {
				$files[] = $dev['css_file_name'];
			}
		}
		return array_unique( $files );
	}
}
