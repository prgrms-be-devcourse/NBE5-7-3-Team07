import React from 'react';

export function Button({ className, variant = "default", size = "default", children, ...props }) {
  const getButtonClasses = () => {
    const baseClass = 'btn';
    const variantClass = `btn-${variant}`;
    const sizeClass = size === 'default' ? 'btn-default-size' : `btn-${size}`;
    
    const combinedClasses = [baseClass, variantClass, sizeClass];
    
    if (className) {
      combinedClasses.push(className);
    }
    
    return combinedClasses.join(' ');
  };

  return (
    <button
      className={getButtonClasses()}
      {...props}
    >
      {children}
    </button>
  );
}
