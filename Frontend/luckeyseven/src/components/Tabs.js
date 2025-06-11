import React from 'react';
import {useRecoilValue} from 'recoil';
import {currentTeamIdState} from '../recoil/atoms/teamAtoms';
import styles from '../styles/Tabs.module.css';

const Tabs = ({activeTab, setActiveTab}) => {
  const teamId = useRecoilValue(currentTeamIdState);
  const tabs = ['Overview', 'Members', 'Expenses', 'Settlement'];

  const getTabPath = (tab) => {
    if (!teamId) {
      return '#';
    } // Or handle the case where teamId is not available

    switch (tab) {
      case 'Expenses':
        return `/teams/${teamId}/expenses`;
        // case 'Settlement':
        //   return `/teams/${teamId}/settlements`;
      default:
        return '#'; // Overview and Members will still use setActiveTab
    }
  };
  return (
      <div className={styles.tabs}>
        {tabs.map(tab => {
          // const isLink = tab === 'Expenses' || tab === 'Settlement';
          const isLink = tab === 'Expenses';
          const path = getTabPath(tab);
          return (
              <button
                  key={tab}
                  to={path}
                  className={`${styles.tabButton} ${activeTab === tab
                      ? styles.tabButtonActive : ''}`}
                  onClick={() => setActiveTab(
                      tab)} // Keep active tab state for styling
              >
                {tab}
              </button>
          );
        })}
      </div>
  );
};

export default Tabs;
