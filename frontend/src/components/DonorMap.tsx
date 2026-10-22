import { useEffect, useRef } from 'react'
import * as L from 'leaflet'
import type { DonorMatch } from '../api/matching'

interface DonorMapProps {
  latitude: number
  longitude: number
  donors: DonorMatch[]
  onSelectDonor: (donor: DonorMatch) => void
}

export function DonorMap({ latitude, longitude, donors, onSelectDonor }: DonorMapProps) {
  const containerRef = useRef<HTMLDivElement>(null)
  const mapRef = useRef<L.Map | null>(null)
  const markerLayerRef = useRef<L.LayerGroup | null>(null)
  const onSelectRef = useRef(onSelectDonor)

  useEffect(() => {
    onSelectRef.current = onSelectDonor
  }, [onSelectDonor])

  useEffect(() => {
    if (!containerRef.current) return

    const map = L.map(containerRef.current).setView([latitude, longitude], 11)
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors',
      maxZoom: 19,
    }).addTo(map)
    markerLayerRef.current = L.layerGroup().addTo(map)
    mapRef.current = map

    return () => {
      map.remove()
      mapRef.current = null
    }
  }, [latitude, longitude])

  useEffect(() => {
    const markerLayer = markerLayerRef.current
    if (!markerLayer) return
    markerLayer.clearLayers()

    const recipientIcon = L.divIcon({
      className: 'recipient-map-marker',
      html: '<span aria-hidden="true">You</span>',
      iconSize: [38, 28],
      iconAnchor: [19, 14],
    })
    L.marker([latitude, longitude], { icon: recipientIcon })
      .bindTooltip('Your approximate location')
      .addTo(markerLayer)

    donors.forEach((donor) => {
      const donorIcon = L.divIcon({
        className: 'donor-map-marker',
        html: '<span aria-hidden="true">+</span>',
        iconSize: [34, 34],
        iconAnchor: [17, 17],
      })
      L.marker([donor.approxLat, donor.approxLng], { icon: donorIcon })
        .bindTooltip(`${donor.bloodGroup} donor · ${donor.distanceKm} km`)
        .on('click', () => onSelectRef.current(donor))
        .addTo(markerLayer)
    })
  }, [latitude, longitude, donors])

  return <div className="donor-map" ref={containerRef} aria-label="Map showing approximate donor locations" />
}
