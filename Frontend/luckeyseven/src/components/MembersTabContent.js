import React from 'react';
import styles from '../styles/MembersTabContent.module.css';

const MembersTabContent = ({ teamCode, teamPassword ,members }) => {
  if (!members || members.length === 0) {
    return <div className={styles.membersTabContent}><h3 className={styles.membersPageTitle}>No members to display.</h3></div>;
  }

  return (
      <div className={styles.membersTabContent}>
        <h3 className={styles.membersPageTitle}>Team members</h3>

        <div className={styles.inviteMembersSection}>
          <h4 className={styles.inviteMembersTitle}>Invite Members</h4>
          <div className={styles.inviteDetails}>
            <span>Team Code: {teamCode}</span>
            <span>Password: {teamPassword}</span>
          </div>
          <button className={styles.copyInviteButton}>Copy Invite Code</button>
        </div>

        <ul className={styles.membersList}>
          {members.map(member => (
              <li key={member.memberId} className={styles.memberItem}> {/* Assuming memberId is unique */}
                <div className={styles.memberAvatar}></div> {/* Placeholder for avatar */}
                <div className={styles.memberInfo}>
                  <span className={styles.memberName}>{member.memberNickName}</span>
                  <span className={styles.memberEmail}>{member.memberEmail}</span>
                </div>
                <span className={`${styles.memberRole} ${member.role === 'Leader' ? styles.leaderRole : styles.memberRoleTag}`}>
                  {member.role}
                </span>
              </li>
          ))}
        </ul>
      </div>
  );
};

export default MembersTabContent;
