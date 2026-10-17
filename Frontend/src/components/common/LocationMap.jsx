import React, { useEffect, useRef, useState } from 'react';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// Fix for default marker icon issues with bundlers
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});

export default function LocationMap({
  latitude,
  longitude,
  address,
  onLocationChange,
  readOnly = false,
  height = '280px'
}) {
  const mapContainerRef = useRef(null);
  const mapInstanceRef = useRef(null);
  const markerRef = useRef(null);
  const [geocoding, setGeocoding] = useState(false);
  const [geoError, setGeoError] = useState(null);

  // Default coordinate: Pune / Central Zone (18.5204, 73.8567)
  const defaultLat = 18.5204;
  const defaultLng = 73.8567;

  const currentLat = latitude ? parseFloat(latitude) : defaultLat;
  const currentLng = longitude ? parseFloat(longitude) : defaultLng;

  useEffect(() => {
    if (!mapContainerRef.current) return;

    if (!mapInstanceRef.current) {
      const map = L.map(mapContainerRef.current, {
        center: [currentLat, currentLng],
        zoom: latitude && longitude ? 15 : 12,
        scrollWheelZoom: !readOnly,
        dragging: !readOnly
      });

      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
        maxZoom: 19
      }).addTo(map);

      const marker = L.marker([currentLat, currentLng], {
        draggable: !readOnly
      }).addTo(map);

      if (!readOnly) {
        marker.on('dragend', async (e) => {
          const { lat, lng } = e.target.getLatLng();
          await handleReverseGeocode(lat, lng);
        });

        map.on('click', async (e) => {
          const { lat, lng } = e.latlng;
          marker.setLatLng([lat, lng]);
          await handleReverseGeocode(lat, lng);
        });
      }

      mapInstanceRef.current = map;
      markerRef.current = marker;
    } else {
      // Update marker position if props changed externally
      if (latitude && longitude) {
        const lat = parseFloat(latitude);
        const lng = parseFloat(longitude);
        if (!isNaN(lat) && !isNaN(lng)) {
          markerRef.current?.setLatLng([lat, lng]);
          mapInstanceRef.current?.setView([lat, lng], 15);
        }
      }
    }

    return () => {
      // Do not destroy on every render, cleanup on unmount
    };
  }, []);

  const handleReverseGeocode = async (lat, lng) => {
    setGeocoding(true);
    setGeoError(null);
    let resolvedAddress = `${lat.toFixed(5)}, ${lng.toFixed(5)}`;
    try {
      const resp = await fetch(
        `https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json`,
        { headers: { 'Accept': 'application/json' } }
      );
      if (resp.ok) {
        const data = await resp.json();
        if (data && data.display_name) {
          resolvedAddress = data.display_name;
        }
      }
    } catch (err) {
      console.warn('Reverse geocoding request failed:', err);
    } finally {
      setGeocoding(false);
      if (onLocationChange) {
        onLocationChange({
          latitude: parseFloat(lat.toFixed(6)),
          longitude: parseFloat(lng.toFixed(6)),
          address: resolvedAddress
        });
      }
    }
  };

  const handleUseMyLocation = () => {
    if (!navigator.geolocation) {
      setGeoError('Geolocation is not supported by your browser.');
      return;
    }

    setGeocoding(true);
    setGeoError(null);

    navigator.geolocation.getCurrentPosition(
      async (pos) => {
        const lat = pos.coords.latitude;
        const lng = pos.coords.longitude;
        if (mapInstanceRef.current && markerRef.current) {
          mapInstanceRef.current.setView([lat, lng], 16);
          markerRef.current.setLatLng([lat, lng]);
        }
        await handleReverseGeocode(lat, lng);
      },
      (err) => {
        setGeocoding(false);
        setGeoError(`Location access denied or unavailable (${err.message}). You can pin manually on the map.`);
      },
      { timeout: 10000, enableHighAccuracy: true }
    );
  };

  return (
    <div className="location-map-wrapper">
      <div className="location-map-header">
        <div className="location-map-info">
          <span className="location-pin-icon">📍</span>
          <span className="location-coord-text">
            {latitude && longitude
              ? `${parseFloat(latitude).toFixed(4)}°, ${parseFloat(longitude).toFixed(4)}°`
              : 'Click map or drag pin to select exact civic location'}
          </span>
          {geocoding && <span className="geocoding-spinner">Resolving address...</span>}
        </div>
        {!readOnly && (
          <button
            type="button"
            className="btn btn-outline btn-sm location-locate-btn"
            onClick={handleUseMyLocation}
            disabled={geocoding}
          >
            🧭 Use My Current Location
          </button>
        )}
      </div>

      {geoError && (
        <div className="location-geo-error">
          <span>⚠️ {geoError}</span>
        </div>
      )}

      <div
        ref={mapContainerRef}
        style={{ width: '100%', height, borderRadius: '8px', zIndex: 1, border: '1px solid #cbd5e1' }}
      />

      {address && (
        <div className="location-resolved-address">
          <strong>Identified Address:</strong> {address}
        </div>
      )}
    </div>
  );
}
