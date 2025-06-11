import { privateApi } from './ApiService';

// Team API
export const getTeamDashboard = async (teamId) => {
    try {
      const response = await privateApi.get(`/api/teams/${teamId}/dashboard`);
      return response.data;
    } catch (error) {
      console.error('Error fetching team dashboard:', error);
      throw error;
    }
  };
  
  export const getTeamMembers = async (teamId) => {
    try {
      const response = await privateApi.get(`/api/teams/${teamId}/members`);
      return response.data;
    } catch (error) {
      console.error('Error fetching team members:', error);
      throw error;
    }
  };
  
  export const createTeam = async (name, teamPassword) => {
    try {
      const response = await privateApi.post('/api/teams', {
        name,
        teamPassword,
      });
      return response.data;
    } catch (error) {
      console.error('Error creating team:', error);
      throw error;
    }
  };
  
  export const joinTeam = async (teamCode, teamPassword) => {
    try {
      const response = await privateApi.post('/api/teams/members', {
        teamCode,
        teamPassword,
      });
      return response.data;
    } catch (error) {
      console.error('Error joining team:', error);
      throw error;
    }
  };
  
  export async function getMyTeams() {
    try {
      const response = await privateApi.get('/api/teams/myTeams');
      return response.data;
    } catch (error) {
      console.error('Error fetching my teams:', error);
      throw error;
    }
  }
  
  export const deleteTeam = async (teamId) => {
    try {
      const response = await privateApi.delete(`/api/teams/${teamId}`);
      return response.data;
    } catch (error) {
      console.error('Error deleting team:', error);
      throw error;
    }
  };