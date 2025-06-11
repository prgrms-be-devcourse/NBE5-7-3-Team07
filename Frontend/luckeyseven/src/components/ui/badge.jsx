import React from 'react';

export function Badge({ className, variant = "default", ...props }) {
  return (
    <div
      className={`badge badge-${variant} ${className || ''}`}
      {...props}
    />
  );
}
