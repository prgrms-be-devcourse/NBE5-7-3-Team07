import React from 'react';

export function Label({ className, ...props }) {
  return (
    <label
      className={`label ${className || ''}`}
      {...props}
    />
  );
}
