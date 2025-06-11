export const SafeFormatterUtil = {
  formatCurrency: (value, fallback = '0') => {
    return (value != null) ? value.toLocaleString() : fallback;
  },
  
  formatNumber: (value, fallback = '0') => {
    return (value != null) ? value.toString() : fallback;
  },
  
  formatPercent: (value, fallback = '0%') => {
    return (value != null) ? `${value}%` : fallback;
  }
};