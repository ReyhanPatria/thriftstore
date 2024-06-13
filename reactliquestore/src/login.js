import React, { useContext, useState } from 'react';
import Container from '@mui/material/Container';
import Typography from '@mui/material/Typography';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { EmployeeContext } from './employeeContext';
import { Box, Grid } from '@mui/material';

const containerStyle = {
  backgroundColor: 'black',
  color: 'white',
  borderRadius: 25,
  boxShadow: '10px 10px 5px grey',
};

const textfieldStyle = {
  input: {
    color: 'white',
    border: '1px solid white',
    borderRadius: '10px',
  },
  placeholder: {
    color: 'lightgray',
  }
};

const btnLogin = {
  marginTop: 5,
  justifyContent: 'center',
  borderRadius: '10px',
  backgroundColor: '#FE8A01',
  color: 'black',
  border: '3px solid black'
};

function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState();
  const navigate = useNavigate();
  const { setEmployeeData } = useContext(EmployeeContext);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post('http://localhost:8080/login', { username, password });
      console.log(response.data[0]);
      redirectBasedOnRole(response.data[0]);
    } catch (error) {
      setError('Invalid username or password');
    }
  };

  const redirectBasedOnRole = (getEmployee) => {
    setEmployeeData(getEmployee);
    switch (getEmployee.accessRight.id) {
      case 1:
        navigate('/admin/pemesanan');
        break;
      case 2:
        navigate('/supervisor/karyawan/presensi');
        break;
      case 3:
        navigate('/manager/dashboard');
        break;
      case 4:
        navigate('/customer/dashboard');
        break;
      default:
        navigate('/login');
    }
  };

  const handleRedirectClick = () => {
    navigate('/manager/dataKaryawan/tambahKaryawan')
  };

  return (
    <Container component="main" maxWidth="sm" style={containerStyle}>
      <Box sx={{ marginTop: 10, display: 'flex', flexDirection: 'column', padding: 5, borderRadius: 5}}>
        <Typography component="h1" variant="h4">Login</Typography>
        <Box sx={{display: 'flex'}}>
          <Typography>Don't have an account?</Typography>&nbsp;&nbsp;&nbsp;
          <Typography sx={{ color: '#FE8A01' }}>Sign up here</Typography>
        </Box>
        <Grid container marginTop={5}>
          <TextField
            sx={textfieldStyle}
            className='input'
            placeholder="Username"
            value={username}
            margin="normal"
            required
            autoComplete='off'
            fullWidth
            onChange={(e) => setUsername(e.target.value)}
          />
          <TextField
            sx={textfieldStyle}
            placeholder="Password"
            variant="outlined"
            type="password"
            value={password}
            margin="normal"
            required
            fullWidth
            onChange={(e) => setPassword(e.target.value)}
          />
          {error && <Typography color="error">{error}</Typography>}
          <Button style={btnLogin} fullWidth onClick={handleSubmit}>Login</Button>
          <Typography sx={{ color: '#FE8A01', marginTop: 3, textAlign: 'center' }}>Forgot Password ?</Typography>
        </Grid>
      </Box>
  </Container>
  );
}

export default LoginPage;
